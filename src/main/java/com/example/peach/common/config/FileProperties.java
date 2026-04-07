package com.example.peach.common.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "peach.file")
// 文件上传相关配置
public class FileProperties {

    private String basePath;
    private String uploadAccessPath;
    private String uploadUrlPrefix;
    private Long imageMaxSize;
    private List<String> allowedImageTypes;
}
