package com.example.peach.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.peach.common.constant.UserType;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.security.SecurityUser;
import com.example.peach.common.utils.JwtUtils;
import com.example.peach.common.utils.SecurityUtils;
import com.example.peach.modules.auth.client.WechatMiniappClient;
import com.example.peach.modules.auth.dto.LoginDTO;
import com.example.peach.modules.auth.dto.MiniappLoginDTO;
import com.example.peach.modules.auth.dto.UpdatePasswordDTO;
import com.example.peach.modules.auth.model.WechatCode2SessionResponse;
import com.example.peach.modules.auth.model.WechatPhoneNumberResponse;
import com.example.peach.modules.auth.service.AuthService;
import com.example.peach.modules.auth.vo.LoginVO;
import com.example.peach.modules.auth.vo.UserInfoVO;
import com.example.peach.modules.user.entity.SysUser;
import com.example.peach.modules.user.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
// 认证业务实现
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final WechatMiniappClient wechatMiniappClient;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           SysUserService sysUserService,
                           PasswordEncoder passwordEncoder,
                           WechatMiniappClient wechatMiniappClient) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.wechatMiniappClient = wechatMiniappClient;
    }

    @Override
    // 后台账号密码登录
    public LoginVO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return buildLoginResult(securityUser.getUser());
    }

    @Override
    // 小程序登录，已有 openId 时跳过手机号验证
    public LoginVO miniappLogin(MiniappLoginDTO dto) {
        log.info("小程序登录请求，loginCode: {}, phoneCode摘要: {}", dto.getLoginCode(), maskCode(dto.getPhoneCode()));

        WechatCode2SessionResponse session = wechatMiniappClient.code2Session(dto.getLoginCode());
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getOpenId, session.getOpenid())
                .eq(SysUser::getDelFlag, 0)
                .one();
        if (user != null) {
            checkUserEnabled(user);
            log.debug("小程序登录命中已有 openId，跳过手机号验证，用户: {}", user.getUsername());
            return buildLoginResult(user);
        }

        if (!hasText(dto.getPhoneCode())) {
            throw new BusinessException(400, "首次登录需要手机号授权");
        }

        WechatPhoneNumberResponse.PhoneInfo phoneInfo = wechatMiniappClient.getPhoneNumber(dto.getPhoneCode());
        user = sysUserService.lambdaQuery()
                .eq(SysUser::getPhone, phoneInfo.getPhoneNumber())
                .eq(SysUser::getDelFlag, 0)
                .one();
        if (user == null) {
            user = createMiniappUser(session.getOpenid(), phoneInfo.getPhoneNumber());
        } else {
            bindOpenId(user.getId(), session.getOpenid());
            user.setOpenId(session.getOpenid());
        }

        checkUserEnabled(user);
        return buildLoginResult(user);
    }

    @Override
    // 纯 JWT 登录，退出时前端删除 token 即可
    public void logout() {
        // 无状态登录不需要服务端处理
    }

    @Override
    // 查询当前登录用户信息
    public UserInfoVO getCurrentUserInfo() {
        SysUser user = sysUserService.getById(SecurityUtils.getUserId());
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setIdentity(resolveIdentity(user.getUserType()));
        vo.setAdmin(UserType.ADMIN.equals(user.getUserType()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改当前用户密码
    public void updatePassword(UpdatePasswordDTO dto) {
        SysUser user = sysUserService.getById(SecurityUtils.getUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserService.updateById(user);
    }

    // 组装统一登录返回结果，不在登录主链路里更新 last_login_time，避免行锁导致登录超时
    private LoginVO buildLoginResult(SysUser user) {
        SecurityUser securityUser = new SecurityUser(user);
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(securityUser));
        vo.setTokenType("Bearer");
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickName(user.getNickName());
        vo.setPhone(user.getPhone());
        vo.setUserType(user.getUserType());
        vo.setIdentity(resolveIdentity(user.getUserType()));
        vo.setAdmin(UserType.ADMIN.equals(user.getUserType()));
        return vo;
    }

    // 只绑定 openId，避免 updateById 整行更新造成不必要的锁等待
    private void bindOpenId(Long userId, String openId) {
        sysUserService.update(Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDelFlag, 0)
                .set(SysUser::getOpenId, openId));
    }

    // 自动创建小程序用户
    private SysUser createMiniappUser(String openId, String phoneNumber) {
        SysUser user = new SysUser();
        user.setUsername(phoneNumber);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickName("微信用户" + phoneNumber.substring(Math.max(phoneNumber.length() - 4, 0)));
        user.setPhone(phoneNumber);
        user.setOpenId(openId);
        user.setStatus(0);
        user.setUserType(UserType.MINIAPP);
        user.setRemark("微信小程序一键登录自动创建");
        user.setDelFlag(0);
        sysUserService.save(user);
        return user;
    }

    // 检查账号是否可用
    private void checkUserEnabled(SysUser user) {
        if (!Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已停用");
        }
    }

    // 转换前端使用的身份标识
    private String resolveIdentity(String userType) {
        return UserType.ADMIN.equals(userType) ? "admin" : "user";
    }

    // 脱敏输出小程序 code，方便排查是否重复或传错值
    private String maskCode(String code) {
        if (code == null || code.isBlank()) {
            return "empty";
        }
        if (code.length() <= 8) {
            return code;
        }
        return code.substring(0, 4) + "..." + code.substring(code.length() - 4);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
