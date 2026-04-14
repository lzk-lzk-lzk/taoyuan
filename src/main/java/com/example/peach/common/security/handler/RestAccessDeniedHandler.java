package com.example.peach.common.security.handler;

import com.example.peach.common.result.Result;
import com.example.peach.common.utils.JsonUtils;
import com.example.peach.common.utils.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
// 无权限访问处理器
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    // 返回统一的无权限 JSON
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ServletUtils.writeJson(response, JsonUtils.toJson(Result.fail(403, "权限不足")));
    }
}
