package com.example.peach.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {

    // JSON 工具类

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
    }

    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
}
