package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// 重置密码参数
public class UserResetPasswordDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 位")
    private String newPassword;
}
