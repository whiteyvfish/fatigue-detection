package com.fatigue.detection.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "detection_records")
@Data
public class DetectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "record_id", unique = true, length = 64)
    private String recordId;        // 业务ID（UUID）

    @Column(name = "user_id", length = 64)
    private String userId;          // 用户标识

    @Column(name = "device_id", length = 64)
    private String deviceId;        // 设备标识

    @Column(name = "detect_type", length = 20)
    private String detectType;      // IMAGE / VIDEO_STREAM

    @Column(name = "result_class", length = 20)
    private String resultClass;     // alert / drowsy

    @Column(name = "confidence")
    private Double confidence;      // 置信度

    @Column(name = "alert_prob")
    private Double alertProb;       // 清醒概率

    @Column(name = "drowsy_prob")
    private Double drowsyProb;      // 疲劳概率

    @Column(name = "file_name", length = 255)
    private String fileName;        // 原始文件名

    @Column(name = "file_path", length = 500)
    private String filePath;        // 存储路径（可选）

    @Column(name = "processing_time")
    private Long processingTime;    // 处理耗时(ms)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "extra_info", length = 1000)
    private String extraInfo;       // 额外信息（JSON格式）

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (recordId == null) {
            recordId = java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
}