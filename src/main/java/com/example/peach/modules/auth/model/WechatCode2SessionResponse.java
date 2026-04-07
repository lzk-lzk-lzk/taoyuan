package com.example.peach.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// 微信 code2Session 响应
public class WechatCode2SessionResponse {

    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    private String unionid;

    private Integer errcode;

    private String errmsg;
}
