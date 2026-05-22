package com.fatigue.detection.service;

import com.fatigue.detection.dto.DetectionResult;
import com.fatigue.detection.entity.DetectionRecord;
import com.fatigue.detection.repository.DetectionRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DetectionService {

    private final AiModelService aiModelService;
    private final DetectionRecordRepository recordRepository;

    @Value("${detection.result.save-path:D:/test_fatigue/results}")
    private String resultSavePath;

    @Value("${detection.result.save-original:false}")
    private boolean saveOriginal;

    public DetectionService(AiModelService aiModelService,
                            DetectionRecordRepository recordRepository) {
        this.aiModelService = aiModelService;
        this.recordRepository = recordRepository;
    }

    @PostConstruct
    public void init() {
        initSavePath();
    }

    private void initSavePath() {
        try {
            Path path = Paths.get(resultSavePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建结果保存目录: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建保存目录失败: {}", e.getMessage());
        }
    }

    @Transactional
    public DetectionRecord detectImage(MultipartFile image, String userId, String deviceId) {
        return detectImage(image, userId, deviceId, false);
    }

    @Transactional
    public DetectionRecord detectImage(MultipartFile image, String userId, String deviceId, boolean visualize) {
        DetectionResult result = aiModelService.detect(image, visualize);

        if (!result.isSuccess()) {
            throw new RuntimeException("检测失败: " + result.getError());
        }

        result.evaluateFatigueState();
        return saveDetectionRecord(image, userId, deviceId, result);
    }

    /**
     * 使用预先计算好的 AI 结果保存检测记录（AI 调用由 Controller 层完成）
     */
    @Transactional
    public DetectionRecord saveDetectionRecord(MultipartFile image, String userId,
                                                String deviceId, DetectionResult result) {
        try {
            return saveDetectionRecord(image.getBytes(), image.getOriginalFilename(), userId, deviceId, result);
        } catch (IOException e) {
            log.error("读取图片失败", e);
            throw new RuntimeException("读取图片失败: " + e.getMessage());
        }
    }

    @Transactional
    public DetectionRecord saveDetectionRecord(byte[] imageBytes, String filename, String userId,
                                                String deviceId, DetectionResult result) {
        long startTime = System.currentTimeMillis();

        // 确保疲劳状态已计算（兜底）
        result.evaluateFatigueState();

        // 后端画框保存结果图（仅当 AI 未返回可视化时）
        String resultImagePath = saveResultImage(imageBytes, filename, result, userId);
        long totalTime = System.currentTimeMillis() - startTime;

        DetectionRecord record = new DetectionRecord();
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setDetectType("IMAGE");
        record.setResultClass(result.getFatigueState());
        record.setConfidence(result.getConfidence() != null ? result.getConfidence() : 0.0);
        record.setAlertProb("normal".equals(result.getFatigueState()) ? 1.0 : 0.0);
        record.setDrowsyProb("drowsy".equals(result.getFatigueState()) ? 1.0 : 0.0);
        record.setFileName(filename);
        record.setFilePath(resultImagePath);
        record.setProcessingTime(result.getProcessingTime() != null ? result.getProcessingTime() : totalTime);

        DetectionRecord saved = recordRepository.save(record);
        result.setRecordId(saved.getRecordId());

        log.info("检测记录已保存，recordId: {}, 类别: {}, 置信度: {}, 检测到 {} 个目标, 结果图: {}",
                saved.getRecordId(), result.getFatigueState(), result.getConfidence(),
                result.getCount(), resultImagePath);

        return saved;
    }

    /**
     * 保存结果图片：画检测框 + 全局状态面板
     */
    private String saveResultImage(MultipartFile originalImage, DetectionResult result, String userId) {
        try {
            return saveResultImage(originalImage.getBytes(), originalImage.getOriginalFilename(), result, userId);
        } catch (IOException e) {
            log.error("读取图片失败", e);
            return null;
        }
    }

    private String saveResultImage(byte[] imageBytes, String originalFilename, DetectionResult result, String userId) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                log.warn("无法读取图片，可能格式不支持");
                return null;
            }

            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            // ========== 遍历 AI 返回的 detections 画框和标签 ==========
            List<DetectionResult.Detection> detections = result.getDetections();
            if (detections != null && !detections.isEmpty()) {
                for (DetectionResult.Detection det : detections) {
                    List<Integer> bbox = det.getBbox();
                    if (bbox == null || bbox.size() < 4) continue;

                    int x1 = Math.max(0, bbox.get(0));
                    int y1 = Math.max(0, bbox.get(1));
                    int x2 = Math.min(image.getWidth(), bbox.get(2));
                    int y2 = Math.min(image.getHeight(), bbox.get(3));

                    // 根据标签判断颜色：疲劳相关标红，正常标绿
                    String labelLower = det.getLabel().toLowerCase();
                    boolean isFatigueSign = labelLower.contains("closed") ||
                            labelLower.contains("drowsy") ||
                            labelLower.contains("yawn") ||
                            labelLower.contains("fatigue");
                    Color boxColor = isFatigueSign ? new Color(220, 20, 60) : new Color(34, 139, 34);

                    // 画检测框
                    g2d.setColor(boxColor);
                    g2d.setStroke(new BasicStroke(Math.max(2, image.getWidth() / 400)));
                    g2d.drawRect(x1, y1, x2 - x1, y2 - y1);

                    // 标签文字
                    String labelText = String.format("%s %.0f%%", det.getLabel(), det.getScore() * 100);
                    int fontSize = Math.max(12, image.getWidth() / 50);
                    Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
                    g2d.setFont(font);
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(labelText);
                    int textHeight = fm.getHeight();

                    // 标签背景（半透明）
                    g2d.setColor(new Color(boxColor.getRed(), boxColor.getGreen(), boxColor.getBlue(), 180));
                    g2d.fillRect(x1, y1 - textHeight, textWidth + 8, textHeight);

                    // 标签文字
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(labelText, x1 + 4, y1 - 4);
                }
            }

            // ========== 全局状态面板 ==========
            String fatigueState = result.getFatigueState();
            boolean isDrowsy = "drowsy".equals(fatigueState);
            boolean isUnknown = "unknown".equals(fatigueState);

            Color bgColor = isDrowsy ? new Color(220, 20, 60, 180) :
                    isUnknown ? new Color(255, 165, 0, 180) :
                            new Color(34, 139, 34, 180);
            Color textColor = Color.WHITE;

            String statusText = isDrowsy ? "⚠️ 疲劳 detected" :
                    isUnknown ? "❓ 状态未知" :
                            "✓ 清醒 normal";
            String confText = String.format("Confidence: %.2f%%",
                    (result.getConfidence() != null ? result.getConfidence() : 0.0) * 100);
            String reasonText = result.getReason() != null ? result.getReason() : "无";
            String timeText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            int fontSize = Math.max(20, image.getWidth() / 20);
            Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int textHeight = fm.getHeight();
            int maxTextWidth = Math.max(
                    fm.stringWidth(statusText),
                    Math.max(fm.stringWidth(confText),
                            Math.max(fm.stringWidth(reasonText), fm.stringWidth(timeText)))
            );

            int padding = 15;
            int boxX = 10;
            int boxY = 10;
            int boxWidth = maxTextWidth + padding * 2;
            int boxHeight = textHeight * 4 + padding * 2;

            g2d.setColor(bgColor);
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            g2d.setColor(isDrowsy ? Color.RED : isUnknown ? Color.ORANGE : Color.GREEN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            g2d.setColor(textColor);
            int textX = boxX + padding;
            int textY = boxY + padding + fm.getAscent();

            g2d.drawString(statusText, textX, textY);
            textY += textHeight;
            g2d.drawString(confText, textX, textY);
            textY += textHeight;
            g2d.drawString("依据: " + reasonText, textX, textY);
            textY += textHeight;
            g2d.drawString(timeText, textX, textY);

            // 用户水印
            if (userId != null) {
                String userText = "User: " + userId;
                g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize / 2));
                FontMetrics fmSmall = g2d.getFontMetrics();
                int userTextWidth = fmSmall.stringWidth(userText);
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.fillRect(image.getWidth() - userTextWidth - 20, image.getHeight() - 30,
                        userTextWidth + 10, 25);
                g2d.setColor(Color.WHITE);
                g2d.drawString(userText, image.getWidth() - userTextWidth - 15, image.getHeight() - 12);
            }

            g2d.dispose();

            // 保存文件（按年月分子目录）
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String resultFlag = isDrowsy ? "DROWSY" : isUnknown ? "UNKNOWN" : "ALERT";
            String randomStr = UUID.randomUUID().toString().substring(0, 8);
            String originalName = originalFilename;
            String baseName = originalName != null ? originalName.replaceAll("\\.[^.]+$", "") : "unknown";
            String fileName = String.format("%s_%s_%s_%s.png", dateStr, baseName, resultFlag, randomStr);

            String subDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path saveDir = Paths.get(resultSavePath, subDir);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }

            Path savePath = saveDir.resolve(fileName);
            ImageIO.write(image, "PNG", savePath.toFile());

            log.info("结果图片已保存: {}, 共 {} 个检测目标", savePath.toAbsolutePath(),
                    detections != null ? detections.size() : 0);
            return savePath.toString();

        } catch (IOException e) {
            log.error("保存结果图片失败: {}", e.getMessage());
            return null;
        }
    }

    public DetectionResult detectFrame(String base64Image) {
        return aiModelService.detectBase64(base64Image);
    }

    public Optional<DetectionRecord> getRecord(String recordId) {
        return recordRepository.findByRecordId(recordId);
    }

    public List<DetectionRecord> getUserRecords(String userId) {
        return recordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<DetectionRecord> getUserRecordsPaged(String userId, int page, int size) {
        return recordRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Map<String, Object> getStatistics(String userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> counts = recordRepository.countByResultClassSince(since);

        Map<String, Object> stats = new HashMap<>();
        stats.put("periodDays", days);

        int total = 0;
        int drowsyCount = 0;
        for (Object[] row : counts) {
            String className = (String) row[0];
            Long count = (Long) row[1];
            stats.put(className + "Count", count);
            total += count.intValue();
            if ("drowsy".equals(className)) {
                drowsyCount = count.intValue();
            }
        }
        stats.put("totalCount", total);

        if (total > 0) {
            stats.put("drowsyRate", String.format("%.2f%%", 100.0 * drowsyCount / total));
        } else {
            stats.put("drowsyRate", "0.00%");
        }

        return stats;
    }
}