package com.example.peach.modules.auth.vo;

import lombok.Data;

@Data
// 当前用户信息返回对象
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickName;
    private String phone;
    private String avatar;
    private String userType;
    private String identity;
    private Boolean admin;
}
