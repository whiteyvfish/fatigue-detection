package com.fatigue.detection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectionResult {

    private boolean success;
    private String error;
    private String device;
    private int count;
    private List<Detection> detections;
    private String reason;

    @JsonProperty("perclos")
    private Double perclos;

    @JsonProperty("blink_freq")
    private Double blinkFreq;

    @JsonProperty("yawn_freq")
    private Double yawnFreq;

    @JsonProperty("fatigue_status")
    private String fatigueStatus;   // 唯一字段，兼容中文"疲劳"/"清醒"和英文"FATIGUE"/"AWAKE"

    @JsonProperty("eye_open")
    private Boolean eyeOpen;

    @JsonProperty("mouth_open")
    private Boolean mouthOpen;

    private String fatigueState;
    private String class_name;
    private Double confidence;
    private Integer processingTime;
    private String recordId;

    @JsonProperty("visualization")
    private String visualization;

    // ========== 手动 getter / setter（彻底避免Lombok自动生成冲突）==========

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<Detection> getDetections() { return detections; }
    public void setDetections(List<Detection> detections) { this.detections = detections; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Double getPerclos() { return perclos; }
    public void setPerclos(Double perclos) { this.perclos = perclos; }

    public Double getBlinkFreq() { return blinkFreq; }
    public void setBlinkFreq(Double blinkFreq) { this.blinkFreq = blinkFreq; }

    public Double getYawnFreq() { return yawnFreq; }
    public void setYawnFreq(Double yawnFreq) { this.yawnFreq = yawnFreq; }

    public String getFatigueStatus() { return fatigueStatus; }
    public void setFatigueStatus(String fatigueStatus) { this.fatigueStatus = fatigueStatus; }

    public Boolean getEyeOpen() { return eyeOpen; }
    public void setEyeOpen(Boolean eyeOpen) { this.eyeOpen = eyeOpen; }

    public Boolean getMouthOpen() { return mouthOpen; }
    public void setMouthOpen(Boolean mouthOpen) { this.mouthOpen = mouthOpen; }

    public String getFatigueState() { return fatigueState; }
    public void setFatigueState(String fatigueState) { this.fatigueState = fatigueState; }

    public String getClass_name() { return class_name; }
    public void setClass_name(String class_name) { this.class_name = class_name; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Integer getProcessingTime() { return processingTime; }
    public void setProcessingTime(Integer processingTime) { this.processingTime = processingTime; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getVisualization() { return visualization; }
    public void setVisualization(String visualization) { this.visualization = visualization; }

    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Detection {
        private String label;
        private double score;
        private List<Integer> bbox;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public List<Integer> getBbox() { return bbox; }
        public void setBbox(List<Integer> bbox) { this.bbox = bbox; }
    }

    public static DetectionResult error(String msg) {
        DetectionResult r = new DetectionResult();
        r.setSuccess(false);
        r.setError(msg);
        return r;
    }

    /**
     * 根据 AI 返回的 detection 数据计算疲劳状态。
     * 置信度优先使用 AI 端返回的 confidence/detections scores，仅当完全缺失时才用合理默认值。
     */
    public void evaluateFatigueState() {
        // 优先使用 AI 端点返回的 fatigueStatus（/analyze_image、/detect_frame 都会返回）
        if (fatigueStatus != null) {
            switch (fatigueStatus) {
                case "FATIGUE":
                case "疲劳":
                    this.fatigueState = "drowsy";
                    this.class_name = "drowsy";
                    break;
                case "AWAKE":
                case "清醒":
                    this.fatigueState = "normal";
                    this.class_name = "normal";
                    break;
                default:
                    this.fatigueState = "unknown";
                    this.class_name = "unknown";
                    break;
            }
        } else {
            // AI 端点未返回 fatigueStatus，根据 detections 推断
            if (detections == null || detections.isEmpty()) {
                this.fatigueState = "unknown";
                this.class_name = "unknown";
                if (this.confidence == null) this.confidence = 0.0;
                return;
            }

            boolean hasClosedEye = detections.stream().anyMatch(d -> {
                String l = d.getLabel();
                return l != null && l.contains("closed");
            });
            boolean hasOpenEye = detections.stream().anyMatch(d -> {
                String l = d.getLabel();
                return l != null && l.contains("open") && l.contains("eye");
            });
            boolean hasOpenMouth = detections.stream().anyMatch(d -> {
                String l = d.getLabel();
                return l != null && (l.contains("Mouth") || l.contains("mouth") || l.contains("yawn"));
            });

            if (hasClosedEye) {
                this.fatigueState = "drowsy";
                this.class_name = "drowsy";
            } else if (hasOpenMouth && !hasOpenEye) {
                this.fatigueState = "drowsy";
                this.class_name = "drowsy";
            } else if (hasOpenEye) {
                this.fatigueState = "normal";
                this.class_name = "normal";
            } else if (!detections.isEmpty()) {
                this.fatigueState = "normal";
                this.class_name = "normal";
            } else {
                this.fatigueState = "unknown";
                this.class_name = "unknown";
            }
        }

        // 置信度：优先使用 AI 端已返回的 confidence
        if (this.confidence == null && detections != null && !detections.isEmpty()) {
            this.confidence = detections.stream()
                    .mapToDouble(Detection::getScore)
                    .max().orElse(0.0);
        }
        if (this.confidence == null) {
            this.confidence = 0.0;
        }
    }
}