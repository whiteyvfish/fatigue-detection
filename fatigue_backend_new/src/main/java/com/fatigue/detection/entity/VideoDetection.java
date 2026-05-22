package com.fatigue.detection.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 视频疲劳检测任务与结果汇总（对应表 video_detection）
 */
@Entity
@Table(name = "video_detection")
@Data
public class VideoDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 业务主键，与目录名 uploads/videos/{videoId} 一致 */
    @Column(name = "video_id", unique = true, nullable = false, length = 64)
    private String videoId;

    @Column(name = "fatigue_ratio")
    private Double fatigueRatio;   // 【新增】疲劳帧占比

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "original_filename", length = 512)
    private String originalFilename;

    /** 磁盘上原始视频绝对或相对路径 */
    @Column(name = "video_path", length = 1024)
    private String videoPath;

    /** 可选：带红框标注的输出视频路径 */
    @Column(name = "annotated_video_path", length = 1024)
    private String annotatedVideoPath;

    /** 视频时长（秒） */
    @Column(name = "duration_sec")
    private Double durationSec;

    @Column(name = "fps")
    private Double fps;

    @Column(name = "total_frames")
    private Integer totalFrames;

    /** 实际参与 AI 检测的采样帧数 */
    @Column(name = "sampled_frames")
    private Integer sampledFrames;

    @Column(name = "fatigue_frames")
    private Integer fatigueFrames;

    /**
     * 合并后的疲劳时间段 JSON，例如：
     * [{"startSec":1.0,"endSec":3.5},{"startSec":10.0,"endSec":10.0}]
     */
    @Column(name = "fatigue_timestamps", columnDefinition = "TEXT")
    private String fatigueTimestamps;

    /** processing / completed / failed */
    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    /** 任务失败时的简要原因（便于前端展示） */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
