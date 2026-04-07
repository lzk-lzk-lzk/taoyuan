package com.example.peach.modules.file.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 文件上传返回对象
public class FileUploadVO {

    private String fileName;
    private String url;
}
