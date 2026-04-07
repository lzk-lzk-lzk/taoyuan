package com.example.peach.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// 微信 access_token 响应
public class WechatAccessTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private Integer errcode;

    private String errmsg;
}
