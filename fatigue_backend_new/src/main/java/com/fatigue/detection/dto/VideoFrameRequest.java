package com.fatigue.detection.dto;

import lombok.Data;

@Data
public class VideoFrameRequest {
    private String imageBase64;     // Base64编码的帧图像
    private Integer frameIndex;     // 帧序号
    private Long timestamp;         // 时间戳
    private String sessionId;       // 会话ID
}