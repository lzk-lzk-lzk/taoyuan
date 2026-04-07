package com.example.peach.modules.file.controller;

import com.example.peach.common.result.Result;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.file.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理")
// 文件上传接口
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传图片")
    // 上传图片文件
    public Result<FileUploadVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(fileStorageService.uploadImage(file));
    }
}
