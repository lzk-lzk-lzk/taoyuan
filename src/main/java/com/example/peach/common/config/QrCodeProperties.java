package com.example.peach.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "peach.qrcode")
// 二维码卡片配置
public class QrCodeProperties {

    private String defaultTargetPrefix;

    private String templatePath;

    private Integer titleCenterX = 626;

    private Integer titleY = 110;

    private Integer qrX = 896;

    private Integer qrY = 195;

    private Integer qrSize = 268;

    private Integer introX = 86;

    private Integer introY = 252;

    private Integer introMaxWidth = 620;

    private Integer introLineHeight = 48;

    private Integer introMaxRows = 3;

    private Integer typeValueX = 156;

    private Integer typeValueY = 458;

    private Integer areaValueX = 206;

    private Integer areaValueY = 582;
}
