package com.example.peach.modules.auth.client;

import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.utils.JsonUtils;
import com.example.peach.modules.auth.config.WechatMiniappProperties;
import com.example.peach.modules.auth.model.WechatAccessTokenResponse;
import com.example.peach.modules.auth.model.WechatCode2SessionResponse;
import com.example.peach.modules.auth.model.WechatPhoneNumberResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
// 微信小程序接口调用客户端
public class WechatMiniappClient {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(15);

    private final RestClient restClient;
    private final WechatMiniappProperties properties;
    private final HttpClient httpClient;

    private volatile String cachedAccessToken;
    private volatile Instant accessTokenExpireAt = Instant.EPOCH;

    public WechatMiniappClient(RestClient restClient, WechatMiniappProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
    }

    // 调用 code2Session 获取 openid
    public WechatCode2SessionResponse code2Session(String loginCode) {
        validateConfig();
        String requestUrl = properties.getCode2SessionUrl()
                + "?appid=" + encode(properties.getAppId())
                + "&secret=" + encode(properties.getAppSecret())
                + "&js_code=" + encode(loginCode)
                + "&grant_type=authorization_code";
        log.info("调用微信 code2Session, 请求URL: {}", requestUrl);
        String body = restClient.get()
                .uri(properties.getCode2SessionUrl()
                                + "?appid={appid}&secret={secret}&js_code={jsCode}&grant_type=authorization_code",
                        properties.getAppId(), properties.getAppSecret(), loginCode)
                .retrieve()
                .body(String.class);
        log.info("微信 code2Session 原始响应: {}", body);
        log.debug("微信 code2Session 响应: {}", body);
        if (body == null || body.isBlank()) {
            throw new BusinessException("微信登录失败：code2Session 无返回结果");
        }
        WechatCode2SessionResponse response = parse(body, WechatCode2SessionResponse.class, "code2Session");
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            throw new BusinessException("微信登录失败：" + response.getErrmsg());
        }
        return response;
    }

    // 通过手机号 code 获取手机号
    public WechatPhoneNumberResponse.PhoneInfo getPhoneNumber(String phoneCode) {
        validateConfig();
        boolean cacheHit = hasValidAccessToken();
        String accessToken = getAccessToken(false);
        String requestUrl = properties.getPhoneNumberUrl() + "?access_token=" + encode(accessToken);
        String requestBody = JsonUtils.toJson(Map.of("code", phoneCode));
        log.info("调用微信 getPhoneNumber, 请求URL: {}, 请求体: {}", requestUrl, requestBody);
        log.debug("开始调用微信 getPhoneNumber, appId: {}, phoneCode长度: {}, phoneCode摘要: {}, accessToken缓存命中: {}, accessToken摘要: {}",
                properties.getAppId(), phoneCode == null ? 0 : phoneCode.length(), maskPhoneCode(phoneCode),
                cacheHit, maskToken(accessToken));

        HttpResult result = sendPhoneNumberRequest(requestUrl, requestBody);
        if (result.statusCode() == 412) {
            log.warn("微信 getPhoneNumber 返回 412，准备刷新 access_token 后重试，appId: {}", properties.getAppId());
            String refreshedToken = getAccessToken(true);
            String retryUrl = properties.getPhoneNumberUrl() + "?access_token=" + encode(refreshedToken);
            log.info("调用微信 getPhoneNumber 重试, 请求URL: {}, 请求体: {}", retryUrl, requestBody);
            result = sendPhoneNumberRequest(retryUrl, requestBody);
        }

        log.info("微信 getPhoneNumber 原始响应, status: {}, body: {}", result.statusCode(), result.body());
        if (result.statusCode() >= 400) {
            throw buildPhoneRequestException(result.statusCode(), result.body());
        }
        if (result.body() == null || result.body().isBlank()) {
            throw new BusinessException("获取微信手机号失败：接口无返回结果");
        }
        WechatPhoneNumberResponse response = parse(result.body(), WechatPhoneNumberResponse.class, "getPhoneNumber");
        if (response.getErrcode() != null && response.getErrcode() != 0) {
            throw new BusinessException("获取微信手机号失败：" + response.getErrmsg());
        }
        if (response.getPhoneInfo() == null || response.getPhoneInfo().getPhoneNumber() == null) {
            throw new BusinessException("获取微信手机号失败：手机号为空");
        }
        return response.getPhoneInfo();
    }

    // 获取并缓存微信 access_token
    private String getAccessToken(boolean forceRefresh) {
        if (!forceRefresh && hasValidAccessToken()) {
            return cachedAccessToken;
        }
        synchronized (this) {
            if (!forceRefresh && hasValidAccessToken()) {
                return cachedAccessToken;
            }
            if (forceRefresh) {
                log.debug("强制刷新微信 access_token, appId: {}", properties.getAppId());
            }
            String requestUrl = properties.getAccessTokenUrl()
                    + "?grant_type=client_credential&appid=" + encode(properties.getAppId())
                    + "&secret=" + encode(properties.getAppSecret());
            log.info("调用微信 access_token, 请求URL: {}", requestUrl);
            String body = restClient.get()
                    .uri(properties.getAccessTokenUrl()
                                    + "?grant_type=client_credential&appid={appid}&secret={secret}",
                            properties.getAppId(), properties.getAppSecret())
                    .retrieve()
                    .body(String.class);
            log.info("微信 access_token 原始响应: {}", body);
            log.debug("微信 access_token 响应: {}", body);
            if (body == null || body.isBlank()) {
                throw new BusinessException("获取微信 access_token 失败：接口无返回结果");
            }
            WechatAccessTokenResponse response = parse(body, WechatAccessTokenResponse.class, "access_token");
            if (response.getErrcode() != null && response.getErrcode() != 0) {
                throw new BusinessException("获取微信 access_token 失败：" + response.getErrmsg());
            }
            cachedAccessToken = response.getAccessToken();
            int expiresIn = response.getExpiresIn() == null ? 7200 : response.getExpiresIn();
            accessTokenExpireAt = Instant.now().plusSeconds(Math.max(expiresIn - 300L, 60L));
            return cachedAccessToken;
        }
    }

    // 发送手机号查询请求
    private HttpResult sendPhoneNumberRequest(String requestUrl, String requestBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(HTTP_TIMEOUT)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new HttpResult(response.statusCode(), response.body());
        } catch (IOException e) {
            log.error("微信 getPhoneNumber 请求IO异常, url: {}, body: {}", requestUrl, requestBody, e);
            throw new BusinessException("获取微信手机号失败：HTTP 请求异常");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("微信 getPhoneNumber 请求被中断, url: {}, body: {}", requestUrl, requestBody, e);
            throw new BusinessException("获取微信手机号失败：请求被中断");
        }
    }

    // 构建手机号接口异常提示
    private BusinessException buildPhoneRequestException(int statusCode, String body) {
        if (statusCode == 412) {
            return new BusinessException("获取微信手机号失败：微信返回 HTTP 412，可能是手机号 code 已失效、已被使用，或当前 HTTP 请求方式与微信接口不兼容");
        }
        String suffix = (body == null || body.isBlank()) ? "" : "，响应体：" + body;
        return new BusinessException("获取微信手机号失败：微信返回 HTTP " + statusCode + suffix);
    }

    // 判断当前 access_token 是否仍然有效
    private boolean hasValidAccessToken() {
        return cachedAccessToken != null && Instant.now().isBefore(accessTokenExpireAt);
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

    // 解析微信接口响应
    private <T> T parse(String body, Class<T> clazz, String apiName) {
        try {
            return JsonUtils.parseObject(body, clazz);
        } catch (RuntimeException e) {
            log.error("微信 {} 响应解析失败，原始内容: {}", apiName, body, e);
            throw new BusinessException("微信接口响应解析失败：" + apiName);
        }
    }

    // 对 URL 参数做编码
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    // 脱敏输出手机号 code，方便排查重复提交问题
    private String maskPhoneCode(String phoneCode) {
        if (phoneCode == null || phoneCode.isBlank()) {
            return "empty";
        }
        if (phoneCode.length() <= 8) {
            return phoneCode;
        }
        return phoneCode.substring(0, 4) + "..." + phoneCode.substring(phoneCode.length() - 4);
    }

    // 脱敏输出 access_token，避免完整打印敏感信息
    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "empty";
        }
        if (token.length() <= 12) {
            return token;
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }

    // 保存 HTTP 状态码和响应体
    private record HttpResult(int statusCode, String body) {
    }
}
