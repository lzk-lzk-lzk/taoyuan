package com.example.peach.common.security.filter;

import com.example.peach.common.config.JwtProperties;
import com.example.peach.common.security.SecurityUser;
import com.example.peach.common.utils.JwtUtils;
import com.example.peach.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
// JWT 认证过滤器
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                   JwtProperties jwtProperties,
                                   UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
        this.userDetailsService = userDetailsService;
    }

    @Override
    // 解析请求中的 token 并设置登录上下文
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(jwtProperties.getHeader());
        String tokenPrefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(authHeader) && StringUtils.hasText(tokenPrefix)
                && authHeader.regionMatches(true, 0, tokenPrefix, 0, tokenPrefix.length())) {
            String token = authHeader.substring(tokenPrefix.length()).trim();
            if (!StringUtils.hasText(token)) {
                log.warn("JWT 为空，请求路径: {}", request.getRequestURI());
            } else if (jwtUtils.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtUtils.parseToken(token);
                String username = claims.getSubject();
                SecurityUser userDetails = (SecurityUser) userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("JWT 认证成功，用户: {}，请求路径: {}", username, request.getRequestURI());
            } else {
                log.warn("JWT 校验失败，请求路径: {}", request.getRequestURI());
            }
        } else if (StringUtils.hasText(authHeader)) {
            log.warn("Authorization 头格式不正确，请求路径: {}，header: {}", request.getRequestURI(), authHeader);
        }
        filterChain.doFilter(request, response);
    }
}
