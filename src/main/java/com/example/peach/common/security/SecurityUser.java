package com.example.peach.common.security;

import com.example.peach.modules.user.entity.SysUser;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
// Spring Security 登录用户对象
public class SecurityUser implements UserDetails {

    private final SysUser user;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(SysUser user) {
        this.user = user;
        this.authorities = List.of(new SimpleGrantedAuthority(user.getUserType()));
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getNickName() {
        return user.getNickName();
    }

    public String getUserType() {
        return user.getUserType();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Integer.valueOf(0).equals(user.getStatus());
    }
}
