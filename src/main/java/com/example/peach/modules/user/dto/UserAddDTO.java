package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// 新增用户参数
public class UserAddDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名长度不能超过 30")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过 30")
    private String nickName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;

    private Integer status = 0;

    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "ADMIN|MINIAPP", message = "用户类型只能是 ADMIN 或 MINIAPP")
    private String userType;

    private String remark;
}
