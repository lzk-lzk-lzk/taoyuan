package com.example.peach.common.context;

import lombok.Data;

@Data
// 当前登录用户信息
public class LoginUser {

    private Long userId;
    private String username;
    private String nickName;
    private String userType;
}
