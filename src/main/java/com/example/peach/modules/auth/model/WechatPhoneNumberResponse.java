package com.example.peach.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
// 微信手机号接口响应
public class WechatPhoneNumberResponse {

    @JsonProperty("phone_info")
    private PhoneInfo phoneInfo;

    private Integer errcode;

    private String errmsg;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    // 微信返回的手机号信息
    public static class PhoneInfo {
        private String phoneNumber;
        private String purePhoneNumber;
        private String countryCode;
        private Watermark watermark;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    // 微信返回的水印信息
    public static class Watermark {
        private Long timestamp;
        private String appid;
    }
}
