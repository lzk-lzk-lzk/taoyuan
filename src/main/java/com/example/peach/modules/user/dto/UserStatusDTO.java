package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
// 修改用户状态参数
public class UserStatusDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
