package com.example.peach.modules.user.controller;

import com.example.peach.common.result.Result;
import com.example.peach.modules.user.dto.UserProfileUpdateDTO;
import com.example.peach.modules.user.service.SysUserService;
import com.example.peach.modules.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/profile")
@Tag(name = "小程序个人信息")
// 当前登录用户个人信息接口
public class UserProfileController {

    private final SysUserService sysUserService;

    public UserProfileController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    @Operation(summary = "获取当前登录用户个人信息")
    // 获取当前登录用户个人信息
    public Result<UserProfileVO> getProfile() {
        return Result.success(sysUserService.getCurrentUserProfile());
    }

    @PutMapping
    @Operation(summary = "修改当前登录用户个人信息")
    // 修改当前登录用户头像和昵称
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        sysUserService.updateCurrentUserProfile(dto);
        return Result.success();
    }
}
