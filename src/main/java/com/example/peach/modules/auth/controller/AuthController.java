package com.example.peach.modules.auth.controller;

import com.example.peach.common.result.Result;
import com.example.peach.modules.auth.dto.LoginDTO;
import com.example.peach.modules.auth.dto.MiniappLoginDTO;
import com.example.peach.modules.auth.dto.UpdatePasswordDTO;
import com.example.peach.modules.auth.service.AuthService;
import com.example.peach.modules.auth.vo.LoginVO;
import com.example.peach.modules.auth.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证模块")
// 认证相关接口
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "后台账号密码登录")
    // 管理端账号密码登录
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/miniapp/login")
    @Operation(summary = "微信小程序一键登录")
    // 小程序一键登录
    public Result<LoginVO> miniappLogin(@Valid @RequestBody MiniappLoginDTO dto) {
        return Result.success(authService.miniappLogin(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    // 退出登录
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息")
    // 获取当前登录用户信息
    public Result<UserInfoVO> info() {
        return Result.success(authService.getCurrentUserInfo());
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    // 修改当前登录用户密码
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        authService.updatePassword(dto);
        return Result.success();
    }

    @PostMapping("/changePassword")
    @Operation(summary = "修改密码(别名接口)")
    // 修改当前登录用户密码
    public Result<Void> changePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        authService.updatePassword(dto);
        return Result.success();
    }
}
