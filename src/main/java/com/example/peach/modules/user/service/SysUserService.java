package com.example.peach.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.peach.common.result.PageResult;
import com.example.peach.modules.user.dto.UserAddDTO;
import com.example.peach.modules.user.dto.UserPageQueryDTO;
import com.example.peach.modules.user.dto.UserResetPasswordDTO;
import com.example.peach.modules.user.dto.UserStatusDTO;
import com.example.peach.modules.user.dto.UserUpdateDTO;
import com.example.peach.modules.user.entity.SysUser;
import com.example.peach.modules.user.vo.UserDetailVO;
import com.example.peach.modules.user.vo.UserPageVO;

public interface SysUserService extends IService<SysUser> {

    // 分页查询用户

    PageResult<UserPageVO> pageUsers(UserPageQueryDTO dto);

    // 查询用户详情
    UserDetailVO getUserDetail(Long id);

    // 新增用户
    void addUser(UserAddDTO dto);

    // 修改用户
    void updateUser(UserUpdateDTO dto);

    // 逻辑删除用户
    void deleteUser(Long id);

    // 重置密码
    void resetPassword(UserResetPasswordDTO dto);

    // 修改用户状态
    void updateStatus(UserStatusDTO dto);
}
