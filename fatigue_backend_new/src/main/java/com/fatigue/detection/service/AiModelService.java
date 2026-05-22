package com.fatigue.detection.service;

import com.fatigue.detection.dto.DetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiModelService {

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;
    private final RestTemplate videoRestTemplate;

    public AiModelService(RestTemplate restTemplate,
                          @Qualifier("videoRestTemplate") RestTemplate videoRestTemplate) {
        this.restTemplate = restTemplate;
        this.videoRestTemplate = videoRestTemplate;
    }

    // ==================== 原有接口（保持不变） ====================

    /**
     * 单张图片检测 - 调用 AI 端 /analyze_image
     */
    public DetectionResult detect(MultipartFile image) {
        return detect(image, false);
    }

    /**
     * 单张图片检测（支持可视化）
     * @param image 图片文件
     * @param visualize 是否让 AI 端返回可视化 base64
     */
    public DetectionResult detect(MultipartFile image, boolean visualize) {
        try {
            return detect(image.getBytes(), image.getOriginalFilename(), visualize);
        } catch (IOException e) {
            log.error("读取图片失败", e);
            return DetectionResult.error("读取图片失败: " + e.getMessage());
        }
    }


    /**
     * 视频疲劳分析（直接传 File，避免大文件读入内存）
     */
    public Map<String, Object> analyzeVideo(File videoFile, int sampleRate) {
        try {
            log.info("调用AI服务: {}/analyze_video, 视频文件: {}, sampleRate: {}",
                    aiServiceUrl, videoFile.getAbsolutePath(), sampleRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video", new FileSystemResource(videoFile));
            body.add("sample_rate", String.valueOf(sampleRate));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = videoRestTemplate.exchange(
                    aiServiceUrl + "/analyze_video",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("error", "AI服务返回为空");
                return err;
            }
            if (!result.containsKey("success")) {
                result.put("success", true);
            }
            return result;

        } catch (Exception e) {
            log.error("视频分析调用失败: {}", e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "视频分析失败: " + e.getMessage());
            return err;
        }
    }

    /**
     * 单张图片检测（支持可视化）- 基于 byte[]，避免 MultipartFile 流被重复消费
     * @param imageBytes 图片字节数组
     * @param filename 原始文件名
     * @param visualize 是否让 AI 端返回可视化 base64
     */
    public DetectionResult detect(byte[] imageBytes, String filename, boolean visualize) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("调用AI服务: {}/analyze_image, 图片大小: {} bytes, visualize: {}", aiServiceUrl, imageBytes.length, visualize);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });
            if (visualize) {
                body.add("visualize", "true");
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<DetectionResult> response = restTemplate.exchange(
                    aiServiceUrl + "/analyze_image",
                    HttpMethod.POST,
                    requestEntity,
                    DetectionResult.class
            );

            DetectionResult result = response.getBody();
            if (result == null) {
                log.error("AI服务返回为空");
                return DetectionResult.error("AI服务返回为空");
            }

            result.setProcessingTime((int)(System.currentTimeMillis() - startTime));

            // 使用 AI 端返回的 confidence，仅当缺失时才用默认值
            if (result.getConfidence() == null) {
                // 从 detections 中提取最大 score 作为置信度
                double maxScore = 0.0;
                if (result.getDetections() != null && !result.getDetections().isEmpty()) {
                    maxScore = result.getDetections().stream()
                            .mapToDouble(DetectionResult.Detection::getScore)
                            .max().orElse(0.0);
                }
                result.setConfidence(maxScore);
            }

            result.evaluateFatigueState();

            log.info("图片检测完成，类别: {}, 置信度: {}, 耗时: {}ms, 检测数量: {}, 疲劳状态: {}, visualization是否为空: {}",
                    result.getClass_name(),
                    result.getConfidence(),
                    result.getProcessingTime(),
                    result.getCount(),
                    result.getFatigueState(),
                    result.getVisualization() == null || result.getVisualization().isEmpty());

            return result;

        } catch (Exception e) {
            log.error("AI服务调用失败: {}, 完整异常: ", e.getMessage(), e);
            return DetectionResult.error("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * Base64图片检测 - 调用 /analyze_image_base64（如果AI端有）或转MultipartFile调用/analyze_image
     */
    public DetectionResult detectBase64(String base64Image) {
        long startTime = System.currentTimeMillis();

        try {
            // 处理 data:image/png;base64, 前缀
            String cleanBase64 = base64Image;
            if (base64Image != null && base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",", 2)[1];
            }

            // 将Base64转为MultipartFile，复用detect方法
            byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            MultipartFile multipartFile = new Base64DecodedMultipartFile(imageBytes, "frame.jpg");

            DetectionResult result = detect(multipartFile);
            if (result != null) {
                result.setProcessingTime((int)(System.currentTimeMillis() - startTime));
            }
            return result;

        } catch (Exception e) {
            log.error("Base64检测失败: {}", e.getMessage());
            return DetectionResult.error("检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量检测
     */
    public Map<String, Object> detectBatch(MultipartFile[] images) {
        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;

        for (MultipartFile image : images) {
            DetectionResult result = detect(image);
            Map<String, Object> item = new HashMap<>();
            item.put("filename", image.getOriginalFilename());
            item.put("success", result.isSuccess());

            if (result.isSuccess()) {
                item.put("fatigueState", result.getFatigueState());
                item.put("confidence", result.getConfidence());
                item.put("detections", result.getDetections());
                item.put("count", result.getCount());
                successCount++;
            } else {
                item.put("error", result.getError());
            }
            results.add(item);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", images.length);
        summary.put("successCount", successCount);
        summary.put("results", results);
        return summary;
    }

    /**
     * 视频疲劳分析
     */
    public Map<String, Object> analyzeVideo(MultipartFile video, int sampleRate) {
        try {
            log.info("调用AI服务: {}/analyze_video, 视频大小: {} bytes, sampleRate: {}",
                    aiServiceUrl, video.getSize(), sampleRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video", new MultipartFileResource(video));
            body.add("sample_rate", String.valueOf(sampleRate));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 使用长超时 RestTemplate（视频分析可能需要几分钟）
            ResponseEntity<Map> response = videoRestTemplate.exchange(
                    aiServiceUrl + "/analyze_video",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("error", "AI服务返回为空");
                return err;
            }

            if (!result.containsKey("success")) {
                result.put("success", true);
            }

            return result;

        } catch (Exception e) {
            log.error("视频分析调用失败: {}", e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "视频分析失败: " + e.getMessage());
            return err;
        }
    }

    // ==================== 新增：实时检测接口 ====================

    /**
     * 实时单帧检测（WebSocket 视频流专用）
     * 调用 AI 端 /detect_frame，返回包含 PERCLOS、眨眼频率等完整状态
     */
    public DetectionResult detectRealtimeFrame(MultipartFile image) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("调用AI实时检测: {}/detect_frame, 图片大小: {} bytes", aiServiceUrl, image.getSize());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new MultipartFileResource(image));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<DetectionResult> response = restTemplate.exchange(
                    aiServiceUrl + "/detect_frame",
                    HttpMethod.POST,
                    requestEntity,
                    DetectionResult.class
            );

            DetectionResult result = response.getBody();
            if (result == null) {
                log.error("AI实时检测返回为空");
                return DetectionResult.error("AI实时检测返回为空");
            }

            result.setProcessingTime((int)(System.currentTimeMillis() - startTime));

            // 映射 fatigueStatus 到 fatigueState（兼容前端）
            if (result.getFatigueStatus() != null) {
                result.evaluateFatigueState();
            }

            log.debug("实时检测完成，状态: {}, PERCLOS: {}, 眨眼: {}, 哈欠: {}",
                    result.getFatigueStatus(),
                    result.getPerclos(),
                    result.getBlinkFreq(),
                    result.getYawnFreq());

            return result;

        } catch (Exception e) {
            log.error("实时帧检测失败: {}", e.getMessage());
            return DetectionResult.error("实时检测失败: " + e.getMessage());
        }
    }

    /**
     * Base64实时帧检测（WebSocket专用入口）
     */
    public DetectionResult detectRealtimeFrameFromBase64(String base64Image) {
        try {
            // 处理 data:image/jpeg;base64, 前缀
            String cleanBase64 = base64Image;
            if (base64Image != null && base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",", 2)[1];
            }

            byte[] imageBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            MultipartFile multipartFile = new Base64DecodedMultipartFile(imageBytes, "frame.jpg");

            return detectRealtimeFrame(multipartFile);

        } catch (Exception e) {
            log.error("Base64实时帧检测失败: {}", e.getMessage());
            return DetectionResult.error("Base64解码失败: " + e.getMessage());
        }
    }

    // ==================== 健康检查 ====================

    public boolean healthCheck() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    aiServiceUrl + "/health", Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return "ok".equals(response.getBody().get("status"));
            }
            return false;
        } catch (Exception e) {
            log.warn("AI服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean readyCheck() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    aiServiceUrl + "/ready", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("AI服务就绪检查失败: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 内部工具类 ====================

    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;

        public MultipartFileResource(MultipartFile file) throws IOException {
            super(file.getBytes());
            this.filename = file.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }

    /**
     * 将Base64解码后的字节数组包装为MultipartFile
     */
    private static class Base64DecodedMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;

        public Base64DecodedMultipartFile(byte[] content, String name) {
            this.content = content;
            this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return name; }

        @Override
        public String getContentType() { return "image/jpeg"; }

        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }
}