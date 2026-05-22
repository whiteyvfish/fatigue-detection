package com.fatigue.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传接口与查询接口返回的视频任务概要
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskResponse {
    private String videoId;
    /** processing / completed / failed */
    private String status;
    /** 人类可读摘要 */
    private String resultSummary;
}
