package com.fatigue.detection.config;

import com.fatigue.detection.controller.WebSocketDetectionController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketDetectionController webSocketDetectionController;

    public WebSocketConfig(WebSocketDetectionController webSocketDetectionController) {
        this.webSocketDetectionController = webSocketDetectionController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 【关键】context-path=/api 会自动加前缀，这里不要写 /api
        registry.addHandler(webSocketDetectionController, "/ws/detection")
                .setAllowedOrigins("*");
    }
}