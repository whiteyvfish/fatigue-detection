package com.fatigue.detection.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Value("${ai.service.timeout:30000}")
    private int timeout;

    @Value("${ai.service.video-timeout:300000}")
    private int videoTimeout;

    /**
     * 普通 RestTemplate（图片检测、健康检查等，默认 30s 超时）
     */
    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        return builder
                .requestFactory(() -> factory)
                .build();
    }

    /**
     * 长超时 RestTemplate（视频分析，默认 5 分钟超时）
     * AiModelService 中通过 @Qualifier("videoRestTemplate") 注入
     */
    @Bean("videoRestTemplate")
    public RestTemplate videoRestTemplate(RestTemplateBuilder builder) {
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(5);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionTimeToLive(60, TimeUnit.SECONDS)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(videoTimeout);

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}