package com.fatigue.detection;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
  // 启用异步处理（视频流用）
@EnableAsync(proxyTargetClass = true)
public class DetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(DetectionApplication.class, args);
    }
}