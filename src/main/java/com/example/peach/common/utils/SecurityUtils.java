package com.example.peach.common.utils;

import com.example.peach.common.context.LoginUser;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    // 安全上下文工具类

    private SecurityUtils() {
    }

    // 获取当前登录用户
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(securityUser.getUserId());
        loginUser.setUsername(securityUser.getUsername());
        loginUser.setNickName(securityUser.getNickName());
        loginUser.setUserType(securityUser.getUserType());
        return loginUser;
    }

    // 获取当前登录用户 ID
    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    // 获取当前登录用户名
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    // 获取当前登录用户类型
    public static String getUserType() {
        return getLoginUser().getUserType();
    }
}
