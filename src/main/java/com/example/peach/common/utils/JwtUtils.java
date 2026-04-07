package com.example.peach.common.utils;

import com.example.peach.common.config.JwtProperties;
import com.example.peach.common.constant.SecurityConstants;
import com.example.peach.common.security.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
// JWT 工具类
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // 生成登录 token
    public String generateToken(SecurityUser user) {
        long now = System.currentTimeMillis();
        long expireTime = now + jwtProperties.getExpireSeconds() * 1000;
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(SecurityConstants.USER_ID_CLAIM, user.getUserId())
                .claim(SecurityConstants.USERNAME_CLAIM, user.getUsername())
                .claim(SecurityConstants.USER_TYPE_CLAIM, user.getUserType())
                .issuedAt(new Date(now))
                .expiration(new Date(expireTime))
                .signWith(key)
                .compact();
    }

    // 解析 token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 校验 token 是否有效
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
