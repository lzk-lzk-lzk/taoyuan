package com.example.peach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan({
        "com.example.peach.modules.category.mapper",
        "com.example.peach.modules.qrcode.mapper",
        "com.example.peach.modules.user.mapper",
        "com.example.peach.modules.variety.mapper"
})
@ConfigurationPropertiesScan
// Spring Boot 启动入口
public class PeachApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeachApplication.class, args);
    }
}
