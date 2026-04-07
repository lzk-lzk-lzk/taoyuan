package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// 修改用户参数
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过 30")
    private String nickName;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;

    private Integer status;

    @NotBlank(message = "用户类型不能为空")
    @Pattern(regexp = "ADMIN|MINIAPP", message = "用户类型只能是 ADMIN 或 MINIAPP")
    private String userType;

    private String remark;
}
