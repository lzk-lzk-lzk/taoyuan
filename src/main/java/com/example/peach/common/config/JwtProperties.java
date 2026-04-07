package com.example.peach.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
// JWT 相关配置
public class JwtProperties {

    private String secret;
    private Long expireSeconds;
    private String header;
    private String tokenPrefix;
}
