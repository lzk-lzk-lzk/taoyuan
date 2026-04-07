package com.example.peach.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "peach.qrcode")
// 二维码默认配置
public class QrCodeProperties {

    private String defaultTargetPrefix;
}
