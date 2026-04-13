package com.example.peach.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    // 统一的 JSON 处理器
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            // 忽略微信等接口里多余的字段，避免因为新增字段导致解析失败
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtils() {
    }

    // 对象转 JSON 字符串
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    // JSON 字符串转对象
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
}
