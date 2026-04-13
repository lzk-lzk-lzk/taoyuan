package com.example.peach.modules.user.vo;

import lombok.Data;

@Data
// 当前登录用户个人信息返回对象
public class UserProfileVO {

    private Long id;
    private String username;
    private String nickName;
    private String phone;
    private String avatar;
    private String userType;
    private String identity;
    private Boolean admin;
}
