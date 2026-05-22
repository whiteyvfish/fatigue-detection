package com.fatigue.detection.service;

import com.fatigue.detection.config.VideoDetectionProperties;
import com.fatigue.detection.dto.VideoTaskResponse;
import com.fatigue.detection.entity.VideoDetection;
import com.fatigue.detection.repository.VideoDetectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * 视频上传落盘、创建数据库任务，并触发异步分析
 */
@Service
@Slf4j
public class VideoDetectionService {

    private final VideoDetectionRepository videoDetectionRepository;
    private final VideoDetectionProperties properties;
    private final VideoDetectionAsyncProcessor videoDetectionAsyncProcessor;

    public VideoDetectionService(VideoDetectionRepository videoDetectionRepository,
                                 VideoDetectionProperties properties,
                                 VideoDetectionAsyncProcessor videoDetectionAsyncProcessor) {
        this.videoDetectionRepository = videoDetectionRepository;
        this.properties = properties;
        this.videoDetectionAsyncProcessor = videoDetectionAsyncProcessor;
    }

    /**
     * 校验、保存 mp4、写入 processing 状态并投递异步任务。
     * 注意：此处不加类级事务，确保 {@link VideoDetectionRepository#save} 先提交，
     * 避免异步线程立刻查询时读不到记录。
     */
    public VideoTaskResponse receiveUpload(MultipartFile file, String userId) throws IOException {
        validateUpload(file);

        String videoId = UUID.randomUUID().toString().replace("-", "");
        Path root = Paths.get(properties.getUploadsDir()).toAbsolutePath().normalize();
        Path dir = root.resolve(videoId);
        Files.createDirectories(dir);

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "video.mp4";
        String ext = resolveExtension(original);
        Path target = dir.resolve("source" + ext);
        file.transferTo(target.toFile());

        VideoDetection entity = new VideoDetection();
        entity.setVideoId(videoId);
        entity.setUserId(userId);
        entity.setOriginalFilename(original);
        entity.setVideoPath(target.toString());
        entity.setStatus("processing");
        videoDetectionRepository.save(entity);

        // 事务提交后再异步更稳妥；此处由 Spring 默认在方法结束提交
        videoDetectionAsyncProcessor.analyzeAsync(videoId);

        log.info("视频任务已创建 videoId={} path={}", videoId, target);
        return toResponse(entity);
    }

    public Optional<VideoTaskResponse> findTask(String videoId) {
        return videoDetectionRepository.findByVideoId(videoId).map(this::toResponse);
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传非空的视频文件");
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!name.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
            throw new IllegalArgumentException("当前仅支持 MP4 格式（.mp4 后缀）");
        }
        String ct = file.getContentType();
        if (ct != null && !ct.isEmpty() && !ct.toLowerCase(Locale.ROOT).startsWith("video/")) {
            throw new IllegalArgumentException("Content-Type 非视频类型: " + ct);
        }
    }

    private String resolveExtension(String originalFilename) {
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0) {
            return ".mp4";
        }
        return originalFilename.substring(dot).toLowerCase(Locale.ROOT);
    }

    private VideoTaskResponse toResponse(VideoDetection e) {
        return new VideoTaskResponse(e.getVideoId(), e.getStatus(), buildResultSummary(e));
    }

    /**
     * 根据任务状态生成简短中文摘要，供接口直接展示
     */
    public String buildResultSummary(VideoDetection e) {
        String s = e.getStatus();
        if (s == null) {
            return "";
        }
        if ("processing".equalsIgnoreCase(s)) {
            return "视频已保存，正在后台抽帧并调用 AI 分析…";
        }
        if ("failed".equalsIgnoreCase(s)) {
            return e.getErrorMessage() != null ? e.getErrorMessage() : "分析失败";
        }
        if ("completed".equalsIgnoreCase(s)) {
            int sampled = e.getSampledFrames() != null ? e.getSampledFrames() : 0;
            int fat = e.getFatigueFrames() != null ? e.getFatigueFrames() : 0;
            double pct = sampled == 0 ? 0 : (100.0 * fat / sampled);
            return String.format(Locale.CHINA,
                    "共检测 %d 个采样帧，其中疲劳 %d 帧（约 %.1f%%）",
                    sampled, fat, pct);
        }
        return s;
    }
}
