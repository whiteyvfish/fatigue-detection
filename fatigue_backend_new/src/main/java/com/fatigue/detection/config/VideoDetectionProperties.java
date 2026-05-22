package com.fatigue.detection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 视频检测相关可配置项（application.yml 前缀 video.detection）
 */
@Data
@Component
@ConfigurationProperties(prefix = "video.detection")
public class VideoDetectionProperties {

    /**
     * 抽帧策略：per_second（每秒 1 帧）或 every_n_frames（每 n 帧 1 帧）
     */
    private String strategy = "per_second";

    /** 当 strategy=every_n_frames 时生效 */
    private int everyNFrames = 5;

    /** 合并相邻疲劳采样点的时间间隙阈值（秒） */
    private double mergeGapSeconds = 1.2;

    /** 视频上传根目录 */
    private String uploadsDir = "./uploads/videos";

    /** 是否写出带红框的标注视频 */

    //private String annotatedFilename = "result_annotated.mp4";
    private boolean perSecondStrategy = true;

    /** 是否生成标记视频 */
    private boolean generateAnnotatedVideo = true;

    /** 标记视频文件名 */
    private String annotatedFilename = "annotated.mp4";
    public boolean isPerSecondStrategy() {
        return "per_second".equalsIgnoreCase(strategy);
    }
}
