package com.example.peach.modules.file.service;

import com.example.peach.modules.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // 上传图片并返回访问地址

    FileUploadVO uploadImage(MultipartFile file);

    // 保存二维码图片到本地
    String saveQrCode(byte[] bytes, String fileName);
}
