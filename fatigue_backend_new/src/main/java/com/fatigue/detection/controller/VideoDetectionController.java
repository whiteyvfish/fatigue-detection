package com.fatigue.detection.controller;

import com.fatigue.detection.dto.ApiResponse;
import com.fatigue.detection.dto.VideoTaskResponse;
import com.fatigue.detection.entity.VideoDetection;
import com.fatigue.detection.repository.VideoDetectionRepository;
import com.fatigue.detection.service.VideoDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.File;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/video")
public class VideoDetectionController {

    private final VideoDetectionService videoDetectionService;
    private final VideoDetectionRepository videoDetectionRepository;

    public VideoDetectionController(VideoDetectionService videoDetectionService,
                                    VideoDetectionRepository videoDetectionRepository) {
        this.videoDetectionService = videoDetectionService;
        this.videoDetectionRepository = videoDetectionRepository;
    }

    /**
     * 直接播放标注视频（绕过静态资源映射，最稳妥）
     * GET /api/video/{videoId}/stream
     */
    @GetMapping("/{videoId}/stream")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId) {
        Optional<VideoDetection> opt = videoDetectionRepository.findByVideoId(videoId);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        VideoDetection v = opt.get();
        String path = v.getAnnotatedVideoPath();
        if (path == null || path.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(path);
        if (!file.exists()) {
            log.error("视频文件不存在: {}", path);
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"annotated.mp4\"")
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    @PostMapping("/upload")
    public ApiResponse<VideoTaskResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId) {

        log.info("收到视频上传 name={} size={} userId={}", file.getOriginalFilename(), file.getSize(), userId);
        try {
            VideoTaskResponse data = videoDetectionService.receiveUpload(file, userId);
            return ApiResponse.success(data, "已接收");
        } catch (IllegalArgumentException e) {
            log.warn("上传校验失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (IOException e) {
            log.error("保存视频失败", e);
            return ApiResponse.error(500, "保存视频失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传处理异常", e);
            return ApiResponse.error(500, "上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoTaskResponse> status(@PathVariable String videoId) {
        Optional<VideoTaskResponse> opt = videoDetectionService.findTask(videoId);
        return opt.map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error(404, "任务不存在"));
    }

    /**
     * 查询完整详情（含视频 URL）
     */
    @GetMapping("/{videoId}/detail")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String videoId) {
        Optional<VideoDetection> opt = videoDetectionRepository.findByVideoId(videoId);
        if (!opt.isPresent()) {
            return ApiResponse.error(404, "任务不存在");
        }
        VideoDetection v = opt.get();

        Map<String, Object> m = new HashMap<>();
        m.put("videoId", v.getVideoId());
        m.put("userId", v.getUserId());
        m.put("originalFilename", v.getOriginalFilename());

        // 【关键】本地路径 → 前端可访问 URL
        m.put("videoPath", toUrl(v.getVideoPath()));
        m.put("annotatedVideoPath", toUrl(v.getAnnotatedVideoPath()));

        m.put("durationSec", v.getDurationSec());
        m.put("fps", v.getFps());
        m.put("totalFrames", v.getTotalFrames());
        m.put("sampledFrames", v.getSampledFrames());
        m.put("fatigueFrames", v.getFatigueFrames());
        m.put("fatigueRatio", v.getFatigueRatio());
        m.put("fatigueTimestamps", v.getFatigueTimestamps());
        m.put("status", v.getStatus());
        m.put("createTime", v.getCreateTime());
        m.put("completeTime", v.getCompleteTime());
        m.put("errorMessage", v.getErrorMessage());
        m.put("resultSummary", videoDetectionService.buildResultSummary(v));

        return ApiResponse.success(m);
    }

    /**
     * 绝对路径转 URL：D:\...uploads\videos\abc\annotated.mp4 → /uploads/videos/abc/annotated.mp4
     */
    private String toUrl(String localPath) {
        if (localPath == null || localPath.isEmpty()) {
            return null;
        }
        try {
            Path root = Paths.get(System.getProperty("user.dir"), "uploads")
                    .toAbsolutePath().normalize();
            Path file = Paths.get(localPath).toAbsolutePath().normalize();

            if (file.startsWith(root)) {
                Path relative = root.relativize(file);
                return "/uploads/" + relative.toString().replace("\\", "/");
            }
        } catch (Exception e) {
            log.warn("路径转换异常: {} -> {}", localPath, e.getMessage());
        }
        // fallback
        return localPath.replace("\\", "/")
                .replaceAll("^.*/uploads/", "/uploads/")
                .replaceAll("^.*uploads/", "/uploads/");
    }
}