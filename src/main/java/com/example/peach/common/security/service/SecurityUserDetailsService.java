package com.example.peach.common.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.security.SecurityUser;
import com.example.peach.modules.user.entity.SysUser;
import com.example.peach.modules.user.service.SysUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
// Security 用户加载服务
public class SecurityUserDetailsService implements UserDetailsService {

    private final SysUserService sysUserService;

    public SecurityUserDetailsService(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    // 按用户名加载登录用户
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDelFlag, 0)
                .last("limit 1"));
        if (user == null) {
            throw new UsernameNotFoundException("账号或密码错误");
        }
        if (!Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已停用");
        }
        return new SecurityUser(user);
    }
}
