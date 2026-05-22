package com.fatigue.detection.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DetectionRequest {
    private MultipartFile image;
    private String userId;      // 可选：用户标识
    private String deviceId;    // 可选：设备标识
    private Long timestamp;     // 可选：时间戳
}