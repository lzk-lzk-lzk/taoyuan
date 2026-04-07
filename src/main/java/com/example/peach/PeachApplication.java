package com.example.peach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.example.peach.modules")
@ConfigurationPropertiesScan
// Spring Boot 启动入口
public class PeachApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeachApplication.class, args);
    }
}
