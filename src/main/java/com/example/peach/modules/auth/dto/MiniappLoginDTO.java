package com.example.peach.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
// 小程序一键登录参数
public class MiniappLoginDTO {

    @NotBlank(message = "登录 code 不能为空")
    private String loginCode;

    @NotBlank(message = "手机号 code 不能为空")
    private String phoneCode;
}
