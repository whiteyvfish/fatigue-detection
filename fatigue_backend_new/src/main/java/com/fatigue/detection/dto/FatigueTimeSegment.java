package com.fatigue.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合并后的疲劳时间段（用于写入 video_detection.fatigue_timestamps JSON）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FatigueTimeSegment {
    private double startSec;
    private double endSec;
}
