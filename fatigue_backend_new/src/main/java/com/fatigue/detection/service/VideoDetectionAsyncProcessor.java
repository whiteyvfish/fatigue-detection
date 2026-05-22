package com.fatigue.detection.service;

import com.fatigue.detection.config.VideoAsyncConfig;
import com.fatigue.detection.config.VideoDetectionProperties;
import com.fatigue.detection.entity.VideoDetection;
import com.fatigue.detection.repository.VideoDetectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class VideoDetectionAsyncProcessor {

    private final VideoDetectionRepository videoDetectionRepository;
    private final AiModelService aiModelService;
    private final VideoDetectionProperties properties;

    public VideoDetectionAsyncProcessor(VideoDetectionRepository videoDetectionRepository,
                                        AiModelService aiModelService,
                                        VideoDetectionProperties properties) {
        this.videoDetectionRepository = videoDetectionRepository;
        this.aiModelService = aiModelService;
        this.properties = properties;
    }

    @Async(VideoAsyncConfig.VIDEO_TASK_EXECUTOR)
    public void analyzeAsync(String videoId) {
        log.info("【异步分析】开始 videoId={}", videoId);

        VideoDetection entity = videoDetectionRepository.findByVideoId(videoId).orElse(null);
        if (entity == null) {
            log.error("【异步分析】任务不存在 videoId={}", videoId);
            return;
        }

        try {
            // 1. 读取本地保存的源视频
            String sourcePath = entity.getVideoPath();
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("源视频文件不存在: " + sourcePath);
            }

            // 2. 调用 AI 服务（sampleRate=1 逐帧分析，视频太长可改 2/5）
            Map<String, Object> aiResult = aiModelService.analyzeVideo(sourceFile, 1);

            Boolean success = (Boolean) aiResult.getOrDefault("success", false);
            if (!success) {
                throw new RuntimeException("AI分析失败: " + aiResult.get("error"));
            }

            // 3. 解析统计字段
            int totalFrames = parseInt(aiResult.get("total_frames"));
            int detectedFrames = parseInt(aiResult.get("detected_frames"));
            int fatigueFrames = parseInt(aiResult.get("fatigue_frames"));
            double fatigueRatio = parseDouble(aiResult.get("fatigue_ratio"));
            double durationSec = parseDouble(aiResult.get("duration_sec"));
            String conclusion = (String) aiResult.getOrDefault("conclusion", "AWAKE");

            // 4. 【核心】保存 AI 返回的标注视频 Base64
            String annotatedPath = null;
            String videoBase64 = (String) aiResult.get("videoBase64");
            if (videoBase64 != null && !videoBase64.isEmpty()) {
                String base64Data = videoBase64.replaceFirst("^data:video/[^;]+;base64,", "");
                byte[] videoBytes = Base64.getDecoder().decode(base64Data);

                // 保存到同目录：./uploads/videos/{videoId}/annotated.mp4
                Path dir = Paths.get(sourcePath).getParent();
                Path annotated = dir.resolve(properties.getAnnotatedFilename());
                Files.write(annotated, videoBytes);

                annotatedPath = annotated.toString();
                log.info("【异步分析】标注视频已保存: {}, 大小: {} bytes", annotatedPath, videoBytes.length);
            } else {
                log.warn("【异步分析】AI 未返回 videoBase64");
            }

            // 5. 更新数据库
            entity.setTotalFrames(totalFrames);
            entity.setSampledFrames(detectedFrames);
            entity.setFatigueFrames(fatigueFrames);
            entity.setFatigueRatio(fatigueRatio);
            entity.setDurationSec(durationSec);
            entity.setAnnotatedVideoPath(annotatedPath);
            entity.setStatus("completed");
            entity.setCompleteTime(LocalDateTime.now());
            videoDetectionRepository.save(entity);

            log.info("【异步分析】完成 videoId={} conclusion={} 疲劳占比={}%",
                    videoId, conclusion, String.format("%.1f", fatigueRatio * 100));

        } catch (Exception e) {
            log.error("【异步分析】失败 videoId={}", videoId, e);
            entity.setStatus("failed");
            entity.setErrorMessage(e.getMessage());
            entity.setCompleteTime(LocalDateTime.now());
            videoDetectionRepository.save(entity);
        }
    }

    private int parseInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}