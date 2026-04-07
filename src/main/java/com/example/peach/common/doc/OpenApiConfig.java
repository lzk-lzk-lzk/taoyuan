package com.example.peach.common.doc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// OpenAPI 文档配置
public class OpenApiConfig {

    @Bean
    // 定义 Swagger 文档基础信息和认证方式
    public OpenAPI openAPI() {
        String securitySchemeName = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("桃资源管理系统 API")
                        .version("1.1.0")
                        .description("包含后台登录、微信小程序一键登录、账号管理、品种管理、文件上传、二维码管理"))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
