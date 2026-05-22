package com.fatigue.detection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 视频分析异步线程池，避免大文件阻塞 Tomcat 请求线程
 */
@Configuration
public class VideoAsyncConfig {

    public static final String VIDEO_TASK_EXECUTOR = "videoTaskExecutor";

    @Bean(name = VIDEO_TASK_EXECUTOR)
    public Executor videoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("video-detect-");
        executor.initialize();
        return executor;
    }
}
