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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
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
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            String token = authHeader.substring(jwtProperties.getTokenPrefix().length());
            if (jwtUtils.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtUtils.parseToken(token);
                String username = claims.getSubject();
                SecurityUser userDetails = (SecurityUser) userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
