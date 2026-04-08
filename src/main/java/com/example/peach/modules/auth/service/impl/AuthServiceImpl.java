package com.example.peach.modules.auth.service.impl;

import cn.hutool.core.util.IdUtil;
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
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    @Transactional(rollbackFor = Exception.class)
    // 处理后台账号密码登录
    public LoginVO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return buildLoginResult(securityUser.getUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 处理小程序一键登录
    public LoginVO miniappLogin(MiniappLoginDTO dto) {
        WechatCode2SessionResponse session = wechatMiniappClient.code2Session(dto.getLoginCode());
        WechatPhoneNumberResponse.PhoneInfo phoneInfo = wechatMiniappClient.getPhoneNumber(dto.getPhoneCode());
        SysUser user = sysUserService.lambdaQuery()
                .eq(SysUser::getOpenId, session.getOpenid())
                .eq(SysUser::getDelFlag, 0)
                .one();
        if (user == null) {
            user = sysUserService.lambdaQuery()
                    .eq(SysUser::getPhone, phoneInfo.getPhoneNumber())
                    .eq(SysUser::getUserType, UserType.MINIAPP)
                    .eq(SysUser::getDelFlag, 0)
                    .one();
        }
        if (user == null) {
            user = createMiniappUser(session.getOpenid(), phoneInfo.getPhoneNumber());
        } else if (user.getOpenId() == null || user.getOpenId().isBlank()) {
            user.setOpenId(session.getOpenid());
            sysUserService.updateById(user);
        }
        if (!Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已停用");
        }
        return buildLoginResult(user);
    }

    @Override
    // 纯 JWT 登录，退出时前端删除 token 即可
    public void logout() {
        // 纯 JWT 无状态登录，前端清除 token 即可
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

    // 组装统一登录返回结果
    private LoginVO buildLoginResult(SysUser user) {
        user.setLastLoginTime(LocalDateTime.now());
        sysUserService.updateById(user);
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

    // 自动创建小程序用户
    private SysUser createMiniappUser(String openId, String phoneNumber) {
        SysUser user = new SysUser();
        user.setUsername(generateMiniappUsername(phoneNumber));
        user.setPassword(passwordEncoder.encode(IdUtil.fastSimpleUUID()));
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

    // 生成不重复的小程序用户名
    private String generateMiniappUsername(String phoneNumber) {
        String suffix = phoneNumber.substring(Math.max(phoneNumber.length() - 4, 0));
        String username = "wx_" + suffix;
        if (sysUserService.lambdaQuery().eq(SysUser::getUsername, username).eq(SysUser::getDelFlag, 0).count() == 0) {
            return username;
        }
        return "wx_" + suffix + "_" + IdUtil.fastSimpleUUID().substring(0, 6);
    }

    // 转换前端使用的身份标识
    private String resolveIdentity(String userType) {
        return UserType.ADMIN.equals(userType) ? "admin" : "user";
    }
}
