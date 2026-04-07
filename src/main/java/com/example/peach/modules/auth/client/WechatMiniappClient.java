package com.example.peach.modules.auth.client;

import com.example.peach.common.exception.BusinessException;
import com.example.peach.modules.auth.config.WechatMiniappProperties;
import com.example.peach.modules.auth.model.WechatAccessTokenResponse;
import com.example.peach.modules.auth.model.WechatCode2SessionResponse;
import com.example.peach.modules.auth.model.WechatPhoneNumberResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
// 微信小程序接口调用客户端
public class WechatMiniappClient {

    private final RestClient restClient;
    private final WechatMiniappProperties properties;

    private volatile String cachedAccessToken;
    private volatile Instant accessTokenExpireAt = Instant.EPOCH;

    public WechatMiniappClient(RestClient restClient, WechatMiniappProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    // 调用 code2Session 获取 openid
    public WechatCode2SessionResponse code2Session(String loginCode) {
        validateConfig();
        WechatCode2SessionResponse response = restClient.get()
                .uri(properties.getCode2SessionUrl()
                                + "?appid={appid}&secret={secret}&js_code={jsCode}&grant_type=authorization_code",
                        properties.getAppId(), properties.getAppSecret(), loginCode)
                .retrieve()
                .body(WechatCode2SessionResponse.class);
        if (response == null) {
            throw new BusinessException("微信登录失败：code2Session 无返回结果");
        }
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            throw new BusinessException("微信登录失败：" + response.getErrmsg());
        }
        return response;
    }

    // 通过手机号 code 获取手机号
    public WechatPhoneNumberResponse.PhoneInfo getPhoneNumber(String phoneCode) {
        validateConfig();
        String accessToken = getAccessToken();
        WechatPhoneNumberResponse response = restClient.post()
                .uri(properties.getPhoneNumberUrl() + "?access_token={accessToken}", accessToken)
                .body(Map.of("code", phoneCode))
                .retrieve()
                .body(WechatPhoneNumberResponse.class);
        if (response == null) {
            throw new BusinessException("获取微信手机号失败：接口无返回结果");
        }
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            throw new BusinessException("获取微信手机号失败：" + response.getErrmsg());
        }
        if (response.getPhoneInfo() == null || response.getPhoneInfo().getPhoneNumber() == null) {
            throw new BusinessException("获取微信手机号失败：手机号为空");
        }
        return response.getPhoneInfo();
    }

    // 获取并缓存微信 access_token
    private String getAccessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(accessTokenExpireAt)) {
            return cachedAccessToken;
        }
        synchronized (this) {
            if (cachedAccessToken != null && Instant.now().isBefore(accessTokenExpireAt)) {
                return cachedAccessToken;
            }
            WechatAccessTokenResponse response = restClient.get()
                    .uri(properties.getAccessTokenUrl()
                                    + "?grant_type=client_credential&appid={appid}&secret={secret}",
                            properties.getAppId(), properties.getAppSecret())
                    .retrieve()
                    .body(WechatAccessTokenResponse.class);
            if (response == null) {
                throw new BusinessException("获取微信 access_token 失败：接口无返回结果");
            }
            if (response.getErrcode() != null && response.getErrcode() != 0) {
                throw new BusinessException("获取微信 access_token 失败：" + response.getErrmsg());
            }
            cachedAccessToken = response.getAccessToken();
            int expiresIn = response.getExpiresIn() == null ? 7200 : response.getExpiresIn();
            accessTokenExpireAt = Instant.now().plusSeconds(Math.max(expiresIn - 300L, 60L));
            return cachedAccessToken;
        }
    }

    // 检查微信配置是否完整
    private void validateConfig() {
        if (properties.getAppId() == null || properties.getAppId().isBlank()
                || properties.getAppSecret() == null || properties.getAppSecret().isBlank()
                || properties.getAppId().contains("your-miniapp")
                || properties.getAppSecret().contains("your-miniapp")) {
            throw new BusinessException("请先在 application.yml 中配置微信小程序 appId 和 appSecret");
        }
    }
}
