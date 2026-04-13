package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
// 当前登录用户修改个人信息参数
public class UserProfileUpdateDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称长度不能超过 30")
    private String nickName;

    @Size(max = 255, message = "头像地址长度不能超过 255")
    private String avatar;
}
