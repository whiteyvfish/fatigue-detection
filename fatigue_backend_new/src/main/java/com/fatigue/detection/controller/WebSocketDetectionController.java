package com.fatigue.detection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatigue.detection.dto.DetectionResult;
import com.fatigue.detection.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class WebSocketDetectionController extends TextWebSocketHandler {

    private final AiModelService aiModelService;
    private final ObjectMapper objectMapper;

    private static final int MAX_FPS = 15;
    private static final long FRAME_INTERVAL_MS = 1000 / MAX_FPS;
    private static final long SESSION_TIMEOUT_MS = 120_000;   // 2min no activity = cleanup
    private static final long CLEANUP_INTERVAL_MS = 30_000;   // run cleanup every 30s

    private final ExecutorService inferenceExecutor = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();

    public WebSocketDetectionController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
        this.objectMapper = new ObjectMapper();
        startSessionCleanup();
    }

    private void startSessionCleanup() {
        cleanupExecutor.scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            sessions.entrySet().removeIf(entry -> {
                SessionState st = entry.getValue();
                if (now - st.lastActivityTime > SESSION_TIMEOUT_MS) {
                    log.info("Session {} idle timeout, cleaning up (frames={}, drowsy={})",
                            entry.getKey(), st.frameCount.get(), st.drowsyCount.get());
                    try { st.session.close(); } catch (IOException ignored) {}
                    return true;
                }
                return false;
            });
        }, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        session.setTextMessageSizeLimit(1024 * 1024);
        session.setBinaryMessageSizeLimit(1024 * 1024);

        sessions.put(sessionId, new SessionState(session));
        log.info("WebSocket connected: {}, active sessions: {}", sessionId, sessions.size());

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "connected");
        msg.put("sessionId", sessionId);
        msg.put("maxFps", MAX_FPS);
        msg.put("message", "Real-time detection connected, max " + MAX_FPS + "fps");
        sendMessage(session, msg);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionState state = sessions.get(session.getId());
        if (state == null) return;

        long now = System.currentTimeMillis();

        // frame rate control
        if (now - state.lastProcessTime < FRAME_INTERVAL_MS) {
            return;
        }
        state.lastProcessTime = now;
        state.lastActivityTime = now;

        // async processing
        inferenceExecutor.submit(() -> {
            try {
                processFrame(session, state, message.getPayload());
            } catch (Exception e) {
                log.error("WS frame error session={}", session.getId(), e);
                sendError(session, "Frame error: " + e.getMessage());
            }
        });
    }

    protected void processFrame(WebSocketSession session, SessionState state, String payload) {
        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String base64Image = (String) data.get("data");
            if (base64Image == null) {
                base64Image = (String) data.get("imageBase64");
            }
            Integer frameIndex = (Integer) data.getOrDefault("frameIndex", 0);

            if (base64Image == null || base64Image.isEmpty()) {
                return;
            }

            long startTime = System.currentTimeMillis();
            DetectionResult result = aiModelService.detectRealtimeFrameFromBase64(base64Image);
            long processTime = System.currentTimeMillis() - startTime;

            // AI-side skipped frame -> silently ignore, don't count
            if (result.isSuccess() && result.getCount() == 0 && processTime < 10) {
                return;
            }

            // Valid frame: count it
            state.frameCount.incrementAndGet();

            if (!result.isSuccess()) {
                sendError(session, "Inference failed: " + result.getError());
                return;
            }

            state.totalInferenceTime.addAndGet(processTime);

            // 即时疲劳帧计数（统计面板用，不受显示延迟影响）
            boolean instantFatigue = Boolean.TRUE.equals(result.getInstantFatigue());
            if (instantFatigue) {
                state.drowsyCount.incrementAndGet();
            }

            // 显示用疲劳状态（AI端已做3秒延迟，前端直接使用）
            boolean displayDrowsy = "drowsy".equals(result.getFatigueState()) || "FATIGUE".equals(result.getFatigueStatus());

            // Dedup: status/stats only on change or periodic; bbox pushes every frame
            String currentStatus = displayDrowsy ? "drowsy"
                    : (result.getFatigueStatus() != null ? result.getFatigueStatus() : "unknown");
            boolean statusChanged = !currentStatus.equals(state.lastSentStatus);
            boolean periodicUpdate = (state.frameCount.get() - state.lastSentFrame) >= 30;

            if (displayDrowsy) {
                state.consecutiveDrowsy.incrementAndGet();
            } else {
                state.consecutiveDrowsy.set(0);
            }

            if (statusChanged || periodicUpdate) {
                // Full update: status + stats + bbox
                state.lastSentStatus = currentStatus;
                state.lastSentFrame = state.frameCount.get();

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("class", currentStatus);
                resultMap.put("confidence", result.getConfidence());
                resultMap.put("isDrowsy", displayDrowsy);
                resultMap.put("perclos", result.getPerclos());
                resultMap.put("blinkFreq", result.getBlinkFreq());
                resultMap.put("yawnFreq", result.getYawnFreq());
                resultMap.put("fatigueStatus", result.getFatigueStatus());
                resultMap.put("reason", result.getReason());
                resultMap.put("detections", result.getDetections());

                Map<String, Object> sessionStatsMap = new HashMap<>();
                sessionStatsMap.put("totalFrames", state.frameCount.get());
                sessionStatsMap.put("drowsyFrames", state.drowsyCount.get());
                sessionStatsMap.put("drowsyRate", String.format("%.1f%%", state.getDrowsyRate() * 100));

                Map<String, Object> response = new HashMap<>();
                response.put("type", "detection_result");
                response.put("frameIndex", frameIndex);
                response.put("timestamp", System.currentTimeMillis());
                response.put("inferenceTime", processTime);
                response.put("fps", Math.round(1000.0 / Math.max(processTime, FRAME_INTERVAL_MS)));
                response.put("result", resultMap);
                response.put("sessionStats", sessionStatsMap);

                sendMessage(session, response);
            } else {
                // Status unchanged: push lightweight bbox-only message every frame
                // 【修复】轻量消息也携带统计信息，保证前端面板实时刷新
                Map<String, Object> lightResult = new HashMap<>();
                lightResult.put("class", currentStatus);
                lightResult.put("isDrowsy", displayDrowsy);
                lightResult.put("detections", result.getDetections());

                Map<String, Object> sessionStatsMap = new HashMap<>();
                sessionStatsMap.put("totalFrames", state.frameCount.get());
                sessionStatsMap.put("drowsyFrames", state.drowsyCount.get());
                sessionStatsMap.put("drowsyRate", String.format("%.1f%%", state.getDrowsyRate() * 100));

                Map<String, Object> lightMsg = new HashMap<>();
                lightMsg.put("type", "detection_boxes");
                lightMsg.put("frameIndex", frameIndex);
                lightMsg.put("timestamp", System.currentTimeMillis());
                lightMsg.put("result", lightResult);
                lightMsg.put("sessionStats", sessionStatsMap);   // 【新增】

                sendMessage(session, lightMsg);
            }

            if (state.consecutiveDrowsy.get() >= 5) {
                sendWarning(session, "Continuous fatigue detected, please rest!");
                state.consecutiveDrowsy.set(0);
            }

        } catch (Exception e) {
            log.error("processFrame error: {}", e.getMessage());
            sendError(session, "Error: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SessionState state = sessions.remove(sessionId);

        if (state != null) {
            log.info("WS closed: {}, frames={}, drowsy={}, rate={}",
                    sessionId, state.frameCount.get(),
                    state.drowsyCount.get(),
                    String.format("%.1f%%", state.getDrowsyRate() * 100));
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.error("sendMessage error", e);
        }
    }

    private void sendError(WebSocketSession session, String errorMsg) {
        Map<String, Object> em = new HashMap<>();
        em.put("type", "error");
        em.put("message", errorMsg);
        em.put("timestamp", System.currentTimeMillis());
        sendMessage(session, em);
    }

    private void sendWarning(WebSocketSession session, String warningMsg) {
        Map<String, Object> wm = new HashMap<>();
        wm.put("type", "warning");
        wm.put("level", "high");
        wm.put("message", warningMsg);
        wm.put("timestamp", System.currentTimeMillis());
        sendMessage(session, wm);
    }

    // session state
    private static class SessionState {
        final WebSocketSession session;
        final AtomicInteger frameCount = new AtomicInteger(0);
        final AtomicInteger drowsyCount = new AtomicInteger(0);
        final AtomicLong totalInferenceTime = new AtomicLong(0);
        final AtomicInteger consecutiveDrowsy = new AtomicInteger(0);
        volatile long lastProcessTime = 0;
        volatile long lastActivityTime = System.currentTimeMillis();
        volatile String lastSentStatus = "";   // for dedup
        volatile int lastSentFrame = 0;

        SessionState(WebSocketSession session) {
            this.session = session;
        }

        double getDrowsyRate() {
            int count = frameCount.get();
            return count > 0 ? (double) drowsyCount.get() / count : 0;
        }
    }
}
