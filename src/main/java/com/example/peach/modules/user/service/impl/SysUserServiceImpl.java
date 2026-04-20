package com.example.peach.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.peach.common.constant.UserType;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.result.PageResult;
import com.example.peach.common.utils.SecurityUtils;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.user.dto.UserAddDTO;
import com.example.peach.modules.user.dto.UserPageQueryDTO;
import com.example.peach.modules.user.dto.UserProfileUpdateDTO;
import com.example.peach.modules.user.dto.UserResetPasswordDTO;
import com.example.peach.modules.user.dto.UserStatusDTO;
import com.example.peach.modules.user.dto.UserUpdateDTO;
import com.example.peach.modules.user.entity.SysUser;
import com.example.peach.modules.user.mapper.SysUserMapper;
import com.example.peach.modules.user.service.SysUserService;
import com.example.peach.modules.user.vo.UserDetailVO;
import com.example.peach.modules.user.vo.UserPageVO;
import com.example.peach.modules.user.vo.UserProfileVO;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 用户业务实现
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    // 分页查询用户列表
    public PageResult<UserPageVO> pageUsers(UserPageQueryDTO dto) {
        Page<SysUser> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<SysUser>()
                        .like(StringUtils.hasText(dto.getUsername()), SysUser::getUsername, dto.getUsername())
                        .like(StringUtils.hasText(dto.getNickName()), SysUser::getNickName, dto.getNickName())
                        .like(StringUtils.hasText(dto.getPhone()), SysUser::getPhone, dto.getPhone())
                        .eq(dto.getStatus() != null, SysUser::getStatus, dto.getStatus())
                        .eq(StringUtils.hasText(dto.getUserType()), SysUser::getUserType, dto.getUserType())
                        .eq(SysUser::getDelFlag, 0)
                        .orderByDesc(SysUser::getCreateTime));
        List<UserPageVO> records = page.getRecords().stream().map(this::toPageVo).toList();
        return new PageResult<>(records, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    // 查询用户详情
    public UserDetailVO getUserDetail(Long id) {
        SysUser user = getUserOrThrow(id);
        UserDetailVO vo = new UserDetailVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 新增用户并加密密码
    public void addUser(UserAddDTO dto) {
        checkUsernameUnique(dto.getUsername(), null);
        checkPhoneUnique(dto.getPhone(), null);
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDelFlag(0);
        save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改用户基础信息
    public void updateUser(UserUpdateDTO dto) {
        SysUser user = getUserOrThrow(dto.getId());
        checkPhoneUnique(dto.getPhone(), dto.getId());
        user.setNickName(dto.getNickName());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus());
        user.setUserType(dto.getUserType());
        user.setRemark(dto.getRemark());
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 逻辑删除用户
    public void deleteUser(Long id) {
        if (id.equals(SecurityUtils.getUserId())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 重置指定用户密码
    public void resetPassword(UserResetPasswordDTO dto) {
        SysUser user = getUserOrThrow(dto.getId());
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改指定用户状态
    public void updateStatus(UserStatusDTO dto) {
        SysUser user = getUserOrThrow(dto.getId());
        if (dto.getId().equals(SecurityUtils.getUserId()) && Integer.valueOf(1).equals(dto.getStatus())) {
            throw new BusinessException("不能停用当前登录账号");
        }
        user.setStatus(dto.getStatus());
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改当前登录用户个人信息
    public void updateCurrentUserProfile(UserProfileUpdateDTO dto) {
        SysUser user = getUserOrThrow(SecurityUtils.getUserId());
        user.setNickName(dto.getNickName());
        user.setAvatar(dto.getAvatar());
        updateById(user);
    }

    @Override
    // 获取当前登录用户个人信息
    public UserProfileVO getCurrentUserProfile() {
        SysUser user = getUserOrThrow(SecurityUtils.getUserId());
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        vo.setIdentity(resolveIdentity(user.getUserType()));
        vo.setAdmin(UserType.ADMIN.equals(user.getUserType()));
        return vo;
    }

    // 校验用户名是否重复
    private void checkUsernameUnique(String username, Long excludeId) {
        Long count = lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDelFlag, 0)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    // 校验手机号是否重复
    private void checkPhoneUnique(String phone, Long excludeId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        Long count = lambdaQuery()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getDelFlag, 0)
                .ne(excludeId != null, SysUser::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("手机号已存在");
        }
    }

    // 查询用户，不存在时抛异常
    private SysUser getUserOrThrow(Long id) {
        SysUser user = getById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDelFlag())) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    // 转换分页返回对象
    private UserPageVO toPageVo(SysUser user) {
        UserPageVO vo = new UserPageVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    // 转换前端使用的身份标识
    private String resolveIdentity(String userType) {
        return UserType.ADMIN.equals(userType) ? "admin" : "user";
    }
}
