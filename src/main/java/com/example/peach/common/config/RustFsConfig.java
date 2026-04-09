package com.example.peach.common.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
// RustFS S3 客户端配置
public class RustFsConfig {

    @Bean
    // 创建 S3 兼容客户端
    public S3Client s3Client(FileProperties fileProperties) {
        String endpoint = fileProperties.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "http://127.0.0.1:9000";
        } else if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            endpoint = "http://" + endpoint;
        }
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(fileProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(fileProperties.getAccessKey(), fileProperties.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
