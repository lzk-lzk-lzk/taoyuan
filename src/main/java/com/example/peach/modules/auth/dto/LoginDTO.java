package com.example.peach.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
// 后台账号密码登录参数
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
