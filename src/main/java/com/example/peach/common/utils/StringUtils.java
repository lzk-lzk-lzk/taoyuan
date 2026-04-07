package com.example.peach.common.utils;

public final class StringUtils {

    // 字符串工具类

    private StringUtils() {
    }

    // 判断字符串是否有内容
    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
