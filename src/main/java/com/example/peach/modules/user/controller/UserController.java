package com.example.peach.modules.user.controller;

import com.example.peach.common.result.PageResult;
import com.example.peach.common.result.Result;
import com.example.peach.modules.user.dto.UserAddDTO;
import com.example.peach.modules.user.dto.UserPageQueryDTO;
import com.example.peach.modules.user.dto.UserResetPasswordDTO;
import com.example.peach.modules.user.dto.UserStatusDTO;
import com.example.peach.modules.user.dto.UserUpdateDTO;
import com.example.peach.modules.user.service.SysUserService;
import com.example.peach.modules.user.vo.UserDetailVO;
import com.example.peach.modules.user.vo.UserPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = "用户管理")
// 用户管理接口
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    // 分页查询用户
    public Result<PageResult<UserPageVO>> page(UserPageQueryDTO dto) {
        return Result.success(sysUserService.pageUsers(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情")
    // 查询单个用户详情
    public Result<UserDetailVO> detail(@PathVariable Long id) {
        return Result.success(sysUserService.getUserDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    // 新增用户
    public Result<Void> add(@Valid @RequestBody UserAddDTO dto) {
        sysUserService.addUser(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改用户")
    // 修改用户
    public Result<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        sysUserService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    // 删除用户
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/resetPassword")
    @Operation(summary = "重置用户密码")
    // 重置用户密码
    public Result<Void> resetPassword(@Valid @RequestBody UserResetPasswordDTO dto) {
        sysUserService.resetPassword(dto);
        return Result.success();
    }

    @PutMapping("/status")
    @Operation(summary = "修改用户状态")
    // 修改用户状态
    public Result<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        sysUserService.updateStatus(dto);
        return Result.success();
    }
}
