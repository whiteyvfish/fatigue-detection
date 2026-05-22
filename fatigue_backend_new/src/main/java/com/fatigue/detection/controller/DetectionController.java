package com.fatigue.detection.controller;

import com.fatigue.detection.dto.ApiResponse;
import com.fatigue.detection.dto.DetectionResult;
import com.fatigue.detection.entity.DetectionRecord;
import com.fatigue.detection.service.AiModelService;
import com.fatigue.detection.service.DetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/detection")
@Slf4j
//@CrossOrigin(origins = "*")
public class DetectionController {

    private final DetectionService detectionService;
    private final AiModelService aiModelService;

    public DetectionController(DetectionService detectionService,
                               AiModelService aiModelService) {
        this.detectionService = detectionService;
        this.aiModelService = aiModelService;
    }

    /**
     * 图片检测 - 保存记录并返回结果图
     * POST /api/detection/image
     * @param visualize 是否返回 AI 端标注结果图（base64）
     */
    @PostMapping("/image")
    public ApiResponse<Map<String, Object>> detectImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "visualize", defaultValue = "false") boolean visualize) {

        log.info("收到图片检测请求，文件名: {}, 大小: {} bytes, userId: {}, visualize: {}",
                image.getOriginalFilename(), image.getSize(), userId, visualize);

        try {
            long startTime = System.currentTimeMillis();

            // 先将图片读取到内存，避免 MultipartFile 流被重复消费
            byte[] imageBytes = image.getBytes();
            String filename = image.getOriginalFilename();

            // 第一步：直接调用 AI 服务获取推理结果（含检测框和可视化）
            DetectionResult aiResult = aiModelService.detect(imageBytes, filename, visualize);
            if (!aiResult.isSuccess()) {
                return ApiResponse.error(500, "AI检测失败: " + aiResult.getError());
            }

            // 第二步：保存记录并在后端画框
            DetectionRecord record = detectionService.saveDetectionRecord(
                    imageBytes, filename, userId, deviceId, aiResult);

            long totalTime = System.currentTimeMillis() - startTime;

            Map<String, Object> result = new HashMap<>();
            result.put("recordId", record.getRecordId());
            result.put("resultClass", record.getResultClass());
            result.put("resultClassCn", "drowsy".equals(record.getResultClass()) ? "疲劳" : "清醒");
            result.put("confidence", record.getConfidence());
            result.put("alertProb", record.getAlertProb());
            result.put("drowsyProb", record.getDrowsyProb());
            result.put("processingTime", record.getProcessingTime());
            result.put("totalTime", totalTime);
            result.put("createdAt", record.getCreatedAt());
            result.put("fileName", record.getFileName());

            // 传递 AI 端返回的检测详情（detections、reason、perclos 等）
            result.put("detections", aiResult.getDetections());
            result.put("fatigueStatus", aiResult.getFatigueStatus());
            result.put("reason", aiResult.getReason());
            result.put("perclos", aiResult.getPerclos());
            result.put("blinkFreq", aiResult.getBlinkFreq());
            result.put("yawnFreq", aiResult.getYawnFreq());

            // 结果图：优先使用 AI 端的可视化（OpenCV 绘制），其次使用后端 Java 绘制的
            if (visualize) {
                // AI 端返回的 base64 可视化（OpenCV 标注）
                if (aiResult.getVisualization() != null && !aiResult.getVisualization().isEmpty()) {
                    result.put("resultImageBase64", aiResult.getVisualization());
                    log.info("使用AI端可视化结果，base64长度: {}", aiResult.getVisualization().length());
                } else {
                    log.warn("AI端未返回可视化结果，detections数量: {}, fatigueStatus: {}",
                            aiResult.getDetections() != null ? aiResult.getDetections().size() : 0,
                            aiResult.getFatigueStatus());
                }

                // 后端 Java 绘制的标注图路径和 URL
                if (record.getFilePath() != null) {
                    result.put("resultImagePath", record.getFilePath());
                    String imageUrl = "/api/detection/image/result?path=" +
                            URLEncoder.encode(record.getFilePath(), "UTF-8");
                    result.put("resultImageUrl", imageUrl);

                    // 如果没有 AI 可视化，则用后端绘制的 base64
                    if (aiResult.getVisualization() == null || aiResult.getVisualization().isEmpty()) {
                        try {
                            byte[] resultImageBytes = Files.readAllBytes(Paths.get(record.getFilePath()));
                            String base64 = Base64.getEncoder().encodeToString(resultImageBytes);
                            result.put("resultImageBase64", "data:image/png;base64," + base64);
                        } catch (IOException e) {
                            log.warn("无法读取结果图片为Base64: {}", e.getMessage());
                        }
                    }
                } else {
                    log.warn("后端结果图保存失败，filePath 为 null");
                }
            }

            log.info("检测成功，recordId: {}, 类别: {}, 检测框数: {}",
                    record.getRecordId(), record.getResultClass(),
                    aiResult.getDetections() != null ? aiResult.getDetections().size() : 0);

            return ApiResponse.success(result, "检测成功");

        } catch (Exception e) {
            log.error("图片检测失败", e);
            return ApiResponse.error(500, "检测失败: " + e.getMessage());
        }
    }

    /**
     * 快速检测 - 不保存记录，只返回结果
     * POST /api/detection/quick
     */
    @PostMapping("/quick")
    public ApiResponse<DetectionResult> quickDetect(
            @RequestParam("image") MultipartFile image) {

        log.info("收到快速检测请求，文件名: {}", image.getOriginalFilename());

        try {
            DetectionResult result = aiModelService.detect(image);
            if (result.isSuccess()) {
                result.evaluateFatigueState();
                return ApiResponse.success(result);
            } else {
                return ApiResponse.error(500, result.getError());
            }
        } catch (Exception e) {
            log.error("快速检测失败", e);
            return ApiResponse.error("快速检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量检测（视频关键帧）
     * POST /api/detection/batch
     */
    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> detectBatch(
            @RequestParam("images") MultipartFile[] images) {

        log.info("收到批量检测请求，共 {} 张图片", images.length);

        try {
            Map<String, Object> result = aiModelService.detectBatch(images);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("批量检测失败", e);
            return ApiResponse.error("批量检测失败: " + e.getMessage());
        }
    }

    /**
     * 获取结果图片文件（供前端<img>标签显示）
     * GET /api/detection/image/result?path=xxx
     */
    @GetMapping("/image/result")
    public ResponseEntity<Resource> getResultImage(@RequestParam String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(Paths.get(path));
            if (contentType == null) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("获取结果图片失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据记录ID获取结果图（简化版）
     * GET /api/detection/record/{recordId}/image
     */
    @GetMapping("/record/{recordId}/image")
    public ResponseEntity<Resource> getRecordImage(@PathVariable String recordId) {
        Optional<DetectionRecord> recordOpt = detectionService.getRecord(recordId);
        if (!recordOpt.isPresent() || recordOpt.get().getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        String path = recordOpt.get().getFilePath();
        return getResultImage(path);
    }

    /**
     * 查询单条记录详情
     * GET /api/detection/record/{recordId}
     */
    @GetMapping("/record/{recordId}")
    public ApiResponse<DetectionRecord> getRecord(@PathVariable String recordId) {
        Optional<DetectionRecord> record = detectionService.getRecord(recordId);
        return record.map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "记录不存在"));
    }

    /**
     * 获取用户历史记录（分页）
     * GET /api/detection/history?userId=xxx&page=0&size=20
     */
    @GetMapping("/history")
    public ApiResponse<Page<DetectionRecord>> getHistory(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<DetectionRecord> records = detectionService.getUserRecordsPaged(userId, page, size);
        return ApiResponse.success(records);
    }

    /**
     * 获取用户所有历史记录（不分页）
     * GET /api/detection/history/all?userId=xxx
     */
    @GetMapping("/history/all")
    public ApiResponse<List<DetectionRecord>> getAllHistory(@RequestParam String userId) {
        List<DetectionRecord> records = detectionService.getUserRecords(userId);
        return ApiResponse.success(records);
    }

    /**
     * 获取统计信息
     * GET /api/detection/statistics?userId=xxx&days=7
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(
            @RequestParam String userId,
            @RequestParam(defaultValue = "7") int days) {

        Map<String, Object> stats = detectionService.getStatistics(userId, days);
        return ApiResponse.success(stats);
    }

    /**
     * 服务健康检查
     * GET /api/detection/health
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        boolean aiHealthy = aiModelService.healthCheck();

        log.debug("6666");

        status.put("backend", "UP");
        status.put("backendTime", System.currentTimeMillis());
        status.put("aiService", aiHealthy ? "UP" : "DOWN");

        return ApiResponse.success(status);
    }


    /**
     * 视频疲劳检测
     * POST /api/detection/video
     */
    @PostMapping("/video")
    public ApiResponse<Map<String, Object>> detectVideo(
            @RequestParam("video") MultipartFile video,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "sampleRate", defaultValue = "1") int sampleRate) {

        log.info("收到视频检测请求，文件名: {}, 大小: {} MB, userId: {}",
                video.getOriginalFilename(),
                String.format("%.2f", video.getSize() / 1024.0 / 1024.0),
                userId);

        try {
            Map<String, Object> result = aiModelService.analyzeVideo(video, sampleRate);

            if (Boolean.FALSE.equals(result.get("success"))) {
                return ApiResponse.error(500, (String) result.get("error"));
            }

            // 可在此保存记录到数据库（略）
            result.put("userId", userId);
            return ApiResponse.success(result, "视频分析完成");

        } catch (Exception e) {
            log.error("视频检测失败", e);
            return ApiResponse.error(500, "视频检测失败: " + e.getMessage());
        }
    }
    /**
     * 实时视频帧检测（WebSocket/轮询用）
     * POST /api/detection/frame
     * Body: {"image_base64": "data:image/jpeg;base64,..."}
     */
    @PostMapping("/frame")
    public ApiResponse<Map<String, Object>> detectFrame(
            @RequestBody Map<String, String> body) {

        String base64 = body.get("image_base64");
        if (base64 == null || base64.isEmpty()) {
            return ApiResponse.error(400, "缺少 image_base64");
        }

        try {
            long start = System.currentTimeMillis();
            DetectionResult result = aiModelService.detectBase64(base64);

            if (!result.isSuccess()) {
                return ApiResponse.error(500, result.getError());
            }

            // 计算疲劳状态（复用现有逻辑）
            result.evaluateFatigueState();

            Map<String, Object> data = new HashMap<>();
            data.put("fatigueState", result.getFatigueState());   // drowsy / normal / unknown
            data.put("confidence", result.getConfidence());
            data.put("count", result.getCount());
            data.put("detections", result.getDetections());
            data.put("processingTime", result.getProcessingTime());
            data.put("serverTime", System.currentTimeMillis() - start);

            return ApiResponse.success(data);

        } catch (Exception e) {
            log.error("实时帧检测失败", e);
            return ApiResponse.error(500, "检测失败: " + e.getMessage());
        }
    }
}