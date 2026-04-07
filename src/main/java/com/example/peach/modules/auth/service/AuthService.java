package com.example.peach.modules.auth.service;

import com.example.peach.modules.auth.dto.LoginDTO;
import com.example.peach.modules.auth.dto.MiniappLoginDTO;
import com.example.peach.modules.auth.dto.UpdatePasswordDTO;
import com.example.peach.modules.auth.vo.LoginVO;
import com.example.peach.modules.auth.vo.UserInfoVO;

public interface AuthService {

    // 后台账号密码登录

    LoginVO login(LoginDTO dto);

    // 小程序一键登录
    LoginVO miniappLogin(MiniappLoginDTO dto);

    // 退出登录
    void logout();

    // 获取当前登录用户信息
    UserInfoVO getCurrentUserInfo();

    // 修改密码
    void updatePassword(UpdatePasswordDTO dto);
}
