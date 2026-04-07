package com.example.peach.modules.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wechat.miniapp")
// 微信小程序配置
public class WechatMiniappProperties {

    private String appId;
    private String appSecret;
    private String accessTokenUrl;
    private String code2SessionUrl;
    private String phoneNumberUrl;
}
