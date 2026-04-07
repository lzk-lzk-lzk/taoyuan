package com.example.peach.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// 微信手机号接口响应
public class WechatPhoneNumberResponse {

    @JsonProperty("phone_info")
    private PhoneInfo phoneInfo;

    private Integer errcode;

    private String errmsg;

    @Data
    // 微信返回的手机号信息
    public static class PhoneInfo {
        private String phoneNumber;
        private String purePhoneNumber;
        private String countryCode;
    }
}
