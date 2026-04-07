package com.example.peach.common.security.handler;

import com.example.peach.common.result.Result;
import com.example.peach.common.utils.JsonUtils;
import com.example.peach.common.utils.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
// 未登录访问处理器
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    // 返回统一的未登录 JSON
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ServletUtils.writeJson(response, JsonUtils.toJson(Result.fail(401, "未登录或登录已失效")));
    }
}
