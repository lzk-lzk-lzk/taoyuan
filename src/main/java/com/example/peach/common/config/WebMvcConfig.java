package com.example.peach.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// 静态资源映射配置
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    public WebMvcConfig(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    // 暴露本地上传文件访问路径
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        registry.addResourceHandler(fileProperties.getUploadAccessPath())
                .addResourceLocations("file:" + path + "/");
    }
}
