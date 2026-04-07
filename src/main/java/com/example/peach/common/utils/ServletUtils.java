package com.example.peach.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServletUtils {

    // Servlet 响应工具类

    private ServletUtils() {
    }

    // 向响应流写入 JSON
    public static void writeJson(HttpServletResponse response, String json) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}
