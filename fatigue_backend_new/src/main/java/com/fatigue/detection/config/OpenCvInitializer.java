package com.fatigue.detection.config;

import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 在应用启动时加载 OpenCV 本地库（openpnp 依赖自带各平台二进制）
 */
@Component
@Slf4j
public class OpenCvInitializer {

    private static volatile boolean loaded;

    @PostConstruct
    public void loadOpenCv() {
        loadIfNeeded();
    }

    /**
     * 供异步任务在类加载顺序异常时再次兜底调用
     */
    public static synchronized void loadIfNeeded() {
        if (loaded) {
            return;
        }
        try {
            OpenCV.loadLocally();
            loaded = true;
            log.info("OpenCV 本地库加载成功");
        } catch (Throwable t) {
            log.error("OpenCV 本地库加载失败: {}", t.getMessage());
            throw new IllegalStateException("无法初始化 OpenCV，请检查依赖与运行环境", t);
        }
    }
}
