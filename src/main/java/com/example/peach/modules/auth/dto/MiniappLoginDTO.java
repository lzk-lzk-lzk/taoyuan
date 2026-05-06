package com.example.peach.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
// 小程序一键登录参数
public class MiniappLoginDTO {

    @NotBlank(message = "登录 code 不能为空")
    private String loginCode;

    // 首次绑定手机号时传，已绑定 openId 后可不传
    private String phoneCode;
}
