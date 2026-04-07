package com.example.peach.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
// HTTP 调用客户端配置
public class RestClientConfig {

    @Bean
    // 提供统一的 RestClient Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
