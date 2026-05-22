////package com.fatigue.detection.controller;
////
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.fatigue.detection.dto.DetectionResult;
////import com.fatigue.detection.dto.VideoFrameRequest;
////import com.fatigue.detection.service.DetectionService;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.stereotype.Component;
////import org.springframework.web.socket.CloseStatus;
////import org.springframework.web.socket.TextMessage;
////import org.springframework.web.socket.WebSocketSession;
////import org.springframework.web.socket.handler.TextWebSocketHandler;
////
////import java.io.IOException;
////import java.util.Collections;
////import java.util.HashMap;
////import java.util.Map;
////import java.util.concurrent.ConcurrentHashMap;
////import java.util.concurrent.atomic.AtomicInteger;
////
////@Component
////@Slf4j
////public class WebSocketDetectionController extends TextWebSocketHandler {
////
////    private final DetectionService detectionService;
////    private final ObjectMapper objectMapper;
////
////    // 存储会话统计信息
////    private final Map<String, SessionStats> sessionStatsMap = new ConcurrentHashMap<>();
////
////    public WebSocketDetectionController(DetectionService detectionService) {
////        this.detectionService = detectionService;
////        this.objectMapper = new ObjectMapper();
////    }
////
////    @Override
////    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
////        String sessionId = session.getId();
////        sessionStatsMap.put(sessionId, new SessionStats());
////        log.info("WebSocket连接建立: {}", sessionId);
////
////        // 发送连接成功消息 - 使用 Java 8 兼容写法
////        Map<String, Object> connectedMessage = new HashMap<>();
////        connectedMessage.put("type", "connected");
////        connectedMessage.put("sessionId", sessionId);
////        connectedMessage.put("message", "实时检测服务已连接");
////        sendMessage(session, connectedMessage);
////    }
////
////    @Override
////    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
////        String payload = message.getPayload();
////        String sessionId = session.getId();
////
////        try {
////            VideoFrameRequest request = objectMapper.readValue(payload, VideoFrameRequest.class);
////            SessionStats stats = sessionStatsMap.get(sessionId);
////
////            // 解码Base64并检测
////            long startTime = System.currentTimeMillis();
////            DetectionResult result = detectionService.detectFrame(request.getImageBase64());
////            long processTime = System.currentTimeMillis() - startTime;
////
////            // 更新统计
////            stats.totalFrames.incrementAndGet();
////            if ("drowsy".equals(result.getClass_name())) {
////                stats.drowsyFrames.incrementAndGet();
////            }
////
////            // 构建响应 - 使用 Java 8 兼容写法
////            Map<String, Object> sessionStatsMap = new HashMap<>();
////            sessionStatsMap.put("totalFrames", stats.totalFrames.get());
////            sessionStatsMap.put("drowsyFrames", stats.drowsyFrames.get());
////            sessionStatsMap.put("drowsyRate", String.format("%.2f%%",
////                    100.0 * stats.drowsyFrames.get() / stats.totalFrames.get()));
////
////            Map<String, Object> response = new HashMap<>();
////            response.put("type", "detection_result");
////            response.put("frameIndex", request.getFrameIndex());
////            response.put("timestamp", System.currentTimeMillis());
////            response.put("processingTime", processTime);
////            response.put("result", result);
////            response.put("sessionStats", sessionStatsMap);
////
////            sendMessage(session, response);
////
////            // 如果连续疲劳帧数过多，发送警告 - 使用 Java 8 兼容写法
////            if (stats.drowsyFrames.get() > 10 &&
////                    stats.drowsyFrames.get() > stats.totalFrames.get() * 0.5) {
////                Map<String, Object> warningMessage = new HashMap<>();
////                warningMessage.put("type", "warning");
////                warningMessage.put("message", "检测到持续疲劳状态，请注意休息！");
////                warningMessage.put("drowsyCount", stats.drowsyFrames.get());
////                sendMessage(session, warningMessage);
////            }
////
////        } catch (Exception e) {
////            log.error("处理视频帧失败: {}", e.getMessage());
////            Map<String, Object> errorMessage = new HashMap<>();
////            errorMessage.put("type", "error");
////            errorMessage.put("message", "处理失败: " + e.getMessage());
////            sendMessage(session, errorMessage);
////        }
////    }
////
////    @Override
////    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
////        String sessionId = session.getId();
////        SessionStats stats = sessionStatsMap.remove(sessionId);
////
////        if (stats != null) {
////            log.info("WebSocket连接关闭: {}, 总帧数: {}, 疲劳帧数: {}",
////                    sessionId, stats.totalFrames.get(), stats.drowsyFrames.get());
////
////            // 可选：保存会话摘要到数据库
////            // detectionService.saveStreamSummary(userId, deviceId, ...);
////        }
////    }
////
////    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
////        try {
////            String json = objectMapper.writeValueAsString(message);
////            session.sendMessage(new TextMessage(json));
////        } catch (IOException e) {
////            log.error("发送WebSocket消息失败", e);
////        }
////    }
////
////    // 会话统计内部类
////    private static class SessionStats {
////        AtomicInteger totalFrames = new AtomicInteger(0);
////        AtomicInteger drowsyFrames = new AtomicInteger(0);
////    }
////}
//package com.fatigue.detection.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fatigue.detection.dto.DetectionResult;
//import com.fatigue.detection.service.AiModelService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Component
//@Slf4j
//public class WebSocketDetectionController extends TextWebSocketHandler {
//
//    private final AiModelService aiModelService;
//    private final ObjectMapper objectMapper;
//
//    // 帧率控制：最大15fps（可根据服务器性能调整）
//    private static final int MAX_FPS = 15;
//    private static final long FRAME_INTERVAL_MS = 1000 / MAX_FPS;
//
//    // 线程池处理推理（避免阻塞WebSocket线程）
//    private final ExecutorService inferenceExecutor = Executors.newFixedThreadPool(4);
//
//    // 存储会话状态
//    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();
//
//    public WebSocketDetectionController(AiModelService aiModelService) {
//        this.aiModelService = aiModelService;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String sessionId = session.getId();
//        sessions.put(sessionId, new SessionState(session));
//
//        log.info("WebSocket连接建立: {}, 当前连接数: {}", sessionId, sessions.size());
//
//        // Java 8 兼容写法
//        Map<String, Object> connectedMessage = new HashMap<>();
//        connectedMessage.put("type", "connected");
//        connectedMessage.put("sessionId", sessionId);
//        connectedMessage.put("maxFps", MAX_FPS);
//        connectedMessage.put("message", "实时检测已连接，建议帧率: " + MAX_FPS + "fps");
//        sendMessage(session, connectedMessage);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        SessionState state = sessions.get(session.getId());
//        if (state == null) return;
//
//        long now = System.currentTimeMillis();
//
//        // 帧率控制：丢弃过早的帧
//        if (now - state.lastProcessTime < FRAME_INTERVAL_MS) {
//            return; // 静默丢弃，不返回错误
//        }
//        state.lastProcessTime = now;
//        state.frameCount.incrementAndGet();
//
//        // 异步处理推理（不阻塞WebSocket接收线程）
//        inferenceExecutor.submit(() -> processFrame(session, state, message.getPayload()));
//    }
//
//    @Async
//    protected void processFrame(WebSocketSession session, SessionState state, String payload) {
//        try {
//            // 解析前端传来的Base64图片
//            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
//            String base64Image = (String) data.get("imageBase64");
//            Integer frameIndex = (Integer) data.getOrDefault("frameIndex", 0);
//            log.info("收到消息，payload长度: {}", payload.length());
//            log.debug("payload内容: {}", payload.substring(0, Math.min(100, payload.length())));
//
//            if (base64Image == null || base64Image.isEmpty()) {
//                return;
//            }
//
//            long startTime = System.currentTimeMillis();
//
//            //DetectionResult result = aiModelService.detectBase64(base64Image);
//            // 调用AI服务（复用现有的图片检测接口）
//            DetectionResult result = aiModelService.detectRealtimeFrameFromBase64(base64Image);
//
//// 响应中增加新字段：
//            Map<String, Object> resultMap = new HashMap<>();
//            resultMap.put("class", result.getClass_name());
//            resultMap.put("confidence", result.getConfidence());
//            resultMap.put("isDrowsy", "drowsy".equals(result.getClass_name()));
//            resultMap.put("perclos", result.getPerclos());        // 新增
//            resultMap.put("blinkFreq", result.getBlinkFreq());    // 新增
//            resultMap.put("yawnFreq", result.getYawnFreq());      // 新增
//            resultMap.put("fatigueStatus", result.getFatigueStatus()); // 新增
//            resultMap.put("detections", result.getDetections()); // 检测框数组
//
//            long processTime = System.currentTimeMillis() - startTime;
//
//            if (!result.isSuccess()) {
//                sendError(session, "推理失败: " + result.getError());
//                return;
//            }
//
//            // 更新统计
//            state.totalInferenceTime.addAndGet(processTime);
//            if ("drowsy".equals(result.getClass_name())) {
//                state.drowsyCount.incrementAndGet();
//            }
//
//            // 构建响应（包含性能指标）- Java 8 兼容写法
//            Map<String, Object> resultMap = new HashMap<>();
//            resultMap.put("class", result.getClass_name());
//            resultMap.put("confidence", result.getConfidence());
//            resultMap.put("isDrowsy", "drowsy".equals(result.getClass_name()));
//
//            Map<String, Object> sessionStatsMap = new HashMap<>();
//            sessionStatsMap.put("totalFrames", state.frameCount.get());
//            sessionStatsMap.put("drowsyFrames", state.drowsyCount.get());
//            sessionStatsMap.put("drowsyRate", String.format("%.1f%%", state.getDrowsyRate() * 100));
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("type", "detection_result");
//            response.put("frameIndex", frameIndex);
//            response.put("timestamp", System.currentTimeMillis());
//            response.put("inferenceTime", processTime);
//            response.put("avgInferenceTime", state.getAvgInferenceTime());
//            response.put("fps", Math.round(1000.0 / Math.max(processTime, FRAME_INTERVAL_MS)));
//            response.put("result", resultMap);
//            response.put("sessionStats", sessionStatsMap);
//
//            sendMessage(session, response);
//
//            // 疲劳预警逻辑（连续疲劳帧）
//            if (result.getClass_name().equals("drowsy")) {
//                state.consecutiveDrowsy.incrementAndGet();
//                if (state.consecutiveDrowsy.get() >= 5) { // 连续5帧疲劳
//                    sendWarning(session, "连续检测到疲劳状态，请立即休息！");
//                    state.consecutiveDrowsy.set(0); // 重置避免重复报警
//                }
//            } else {
//                state.consecutiveDrowsy.set(0);
//            }
//
//        } catch (Exception e) {
//            log.error("处理帧失败: {}", e.getMessage());
//            sendError(session, "处理异常: " + e.getMessage());
//        }
//
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        String sessionId = session.getId();
//        SessionState state = sessions.remove(sessionId);
//
//        if (state != null) {
//            log.info("WebSocket连接关闭: {}, 总帧数: {}, 平均推理耗时: {}ms, 疲劳率: {}%",
//                    sessionId,
//                    state.frameCount.get(),
//                    state.getAvgInferenceTime(),
//                    String.format("%.1f", state.getDrowsyRate() * 100));
//        }
//    }
//
//    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
//        try {
//            if (session.isOpen()) {
//                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
//            }
//        } catch (IOException e) {
//            log.error("发送消息失败", e);
//        }
//    }
//
//    private void sendError(WebSocketSession session, String errorMsg) {
//        Map<String, Object> errorMessage = new HashMap<>();
//        errorMessage.put("type", "error");
//        errorMessage.put("message", errorMsg);
//        errorMessage.put("timestamp", System.currentTimeMillis());
//        sendMessage(session, errorMessage);
//    }
//
//    private void sendWarning(WebSocketSession session, String warningMsg) {
//        Map<String, Object> warningMessage = new HashMap<>();
//        warningMessage.put("type", "warning");
//        warningMessage.put("level", "high");
//        warningMessage.put("message", warningMsg);
//        warningMessage.put("timestamp", System.currentTimeMillis());
//        sendMessage(session, warningMessage);
//    }
//
//    // 会话状态内部类
//    private static class SessionState {
//        final WebSocketSession session;
//        final AtomicInteger frameCount = new AtomicInteger(0);
//        final AtomicInteger drowsyCount = new AtomicInteger(0);
//        final AtomicLong totalInferenceTime = new AtomicLong(0);
//        final AtomicInteger consecutiveDrowsy = new AtomicInteger(0);
//        volatile long lastProcessTime = 0;
//
//        SessionState(WebSocketSession session) {
//            this.session = session;
//        }
//
//        double getAvgInferenceTime() {
//            int count = frameCount.get();
//            return count > 0 ? (double) totalInferenceTime.get() / count : 0;
//        }
//
//        double getDrowsyRate() {
//            int count = frameCount.get();
//            return count > 0 ? (double) drowsyCount.get() / count : 0;
//        }
//    }
//}
package com.fatigue.detection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatigue.detection.dto.DetectionResult;
import com.fatigue.detection.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    // 帧率控制：最大15fps
    private static final int MAX_FPS = 15;
    private static final long FRAME_INTERVAL_MS = 1000 / MAX_FPS;

    // 线程池处理推理
    private final ExecutorService inferenceExecutor = Executors.newFixedThreadPool(4);

    // 存储会话状态
    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();

    public WebSocketDetectionController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        // ===== 关键修复：调大消息大小限制，防止 Base64 图片过大导致断开 =====
        session.setTextMessageSizeLimit(1024 * 1024);      // 文本消息 1MB
        session.setBinaryMessageSizeLimit(1024 * 1024);    // 二进制消息 1MB

        sessions.put(sessionId, new SessionState(session));

        log.info("WebSocket连接建立: {}, 当前连接数: {}", sessionId, sessions.size());

        Map<String, Object> connectedMessage = new HashMap<>();
        connectedMessage.put("type", "connected");
        connectedMessage.put("sessionId", sessionId);
        connectedMessage.put("maxFps", MAX_FPS);
        connectedMessage.put("message", "实时检测已连接，建议帧率: " + MAX_FPS + "fps");
        sendMessage(session, connectedMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionState state = sessions.get(session.getId());
        if (state == null) return;

        long now = System.currentTimeMillis();

        // 帧率控制
        if (now - state.lastProcessTime < FRAME_INTERVAL_MS) {
            return;
        }
        state.lastProcessTime = now;
        state.frameCount.incrementAndGet();

        // 【关键】线程池任务包一层 try-catch，防止异常导致连接断开
        inferenceExecutor.submit(() -> {
            try {
                processFrame(session, state, message.getPayload());
            } catch (Exception e) {
                log.error("【WebSocket】异步处理帧异常，session={}", session.getId(), e);
                // 异常只影响这一帧，不关闭连接
                sendError(session, "单帧处理异常: " + e.getMessage());
            }
        });
    }

    @Async
    protected void processFrame(WebSocketSession session, SessionState state, String payload) {
        try {
            // 解析前端传来的JSON
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String base64Image = (String) data.get("data");  // 前端 sendFrame 里用的字段名
            if (base64Image == null) {
                base64Image = (String) data.get("imageBase64"); // 兼容旧字段
            }
            Integer frameIndex = (Integer) data.getOrDefault("frameIndex", 0);

            if (base64Image == null || base64Image.isEmpty()) {
                return;
            }

            long startTime = System.currentTimeMillis();

            // 调用AI实时检测（新接口）
            DetectionResult result = aiModelService.detectRealtimeFrameFromBase64(base64Image);

            long processTime = System.currentTimeMillis() - startTime;

            if (!result.isSuccess()) {
                sendError(session, "推理失败: " + result.getError());
                return;
            }

            // 更新统计
            state.totalInferenceTime.addAndGet(processTime);
            boolean isDrowsy = "drowsy".equals(result.getFatigueState()) || "FATIGUE".equals(result.getFatigueStatus());
            if (isDrowsy) {
                state.drowsyCount.incrementAndGet();
            }

            // 在 processFrame 方法中，替换 resultMap 构建部分：
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("class", result.getFatigueState() != null ? result.getFatigueState() : "unknown");
            resultMap.put("confidence", result.getConfidence());
            resultMap.put("isDrowsy", "drowsy".equals(result.getFatigueState()) || "FATIGUE".equals(result.getFatigueStatus()));

// 关键：现在这些字段能正确拿到了
            resultMap.put("perclos", result.getPerclos());
            resultMap.put("blinkFreq", result.getBlinkFreq());
            resultMap.put("yawnFreq", result.getYawnFreq());
            resultMap.put("fatigueStatus", result.getFatigueStatus());
            resultMap.put("reason", result.getReason());
            resultMap.put("detections", result.getDetections());  // 检测框数组给前端画框

            Map<String, Object> sessionStatsMap = new HashMap<>();
            sessionStatsMap.put("totalFrames", state.frameCount.get());
            sessionStatsMap.put("drowsyFrames", state.drowsyCount.get());
            sessionStatsMap.put("drowsyRate", String.format("%.1f%%", state.getDrowsyRate() * 100));

            Map<String, Object> response = new HashMap<>();
            response.put("type", "detection_result");
            response.put("frameIndex", frameIndex);
            response.put("timestamp", System.currentTimeMillis());
            response.put("inferenceTime", processTime);
            response.put("avgInferenceTime", state.getAvgInferenceTime());
            response.put("fps", Math.round(1000.0 / Math.max(processTime, FRAME_INTERVAL_MS)));
            response.put("result", resultMap);
            response.put("sessionStats", sessionStatsMap);

            sendMessage(session, response);

            // 疲劳预警逻辑
            if (isDrowsy) {
                state.consecutiveDrowsy.incrementAndGet();
                if (state.consecutiveDrowsy.get() >= 5) {
                    sendWarning(session, "连续检测到疲劳状态，请立即休息！");
                    state.consecutiveDrowsy.set(0);
                }
            } else {
                state.consecutiveDrowsy.set(0);
            }

        } catch (Exception e) {
            log.error("处理帧失败: {}", e.getMessage());
            sendError(session, "处理异常: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SessionState state = sessions.remove(sessionId);

        if (state != null) {
            log.info("WebSocket连接关闭: {}, 总帧数: {}, 平均推理耗时: {}ms, 疲劳率: {}%",
                    sessionId,
                    state.frameCount.get(),
                    state.getAvgInferenceTime(),
                    String.format("%.1f", state.getDrowsyRate() * 100));
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    private void sendError(WebSocketSession session, String errorMsg) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("type", "error");
        errorMessage.put("message", errorMsg);
        errorMessage.put("timestamp", System.currentTimeMillis());
        sendMessage(session, errorMessage);
    }

    private void sendWarning(WebSocketSession session, String warningMsg) {
        Map<String, Object> warningMessage = new HashMap<>();
        warningMessage.put("type", "warning");
        warningMessage.put("level", "high");
        warningMessage.put("message", warningMsg);
        warningMessage.put("timestamp", System.currentTimeMillis());
        sendMessage(session, warningMessage);
    }

    // 会话状态内部类
    private static class SessionState {
        final WebSocketSession session;
        final AtomicInteger frameCount = new AtomicInteger(0);
        final AtomicInteger drowsyCount = new AtomicInteger(0);
        final AtomicLong totalInferenceTime = new AtomicLong(0);
        final AtomicInteger consecutiveDrowsy = new AtomicInteger(0);
        volatile long lastProcessTime = 0;

        SessionState(WebSocketSession session) {
            this.session = session;
        }

        double getAvgInferenceTime() {
            int count = frameCount.get();
            return count > 0 ? (double) totalInferenceTime.get() / count : 0;
        }

        double getDrowsyRate() {
            int count = frameCount.get();
            return count > 0 ? (double) drowsyCount.get() / count : 0;
        }
    }
}