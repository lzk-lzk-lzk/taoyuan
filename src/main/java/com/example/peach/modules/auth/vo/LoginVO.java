package com.example.peach.modules.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 登录返回信息
public class LoginVO {

    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String nickName;
    private String phone;
    private String userType;
    private String identity;
    private Boolean admin;
}
