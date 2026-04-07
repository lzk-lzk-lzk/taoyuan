package com.example.peach.modules.file.service.impl;

import com.example.peach.common.config.FileProperties;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.file.vo.FileUploadVO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
// 本地文件存储实现
public class FileStorageServiceImpl implements FileStorageService {

    private final FileProperties fileProperties;

    public FileStorageServiceImpl(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    // 上传图片到本地目录
    public FileUploadVO uploadImage(MultipartFile file) {
        validateImage(file);
        String extension = getExtension(file.getOriginalFilename());
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path relativePath = Paths.get("images", dateDir, fileName);
        Path target = resolvePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }
        return new FileUploadVO(fileName, buildAccessUrl(relativePath));
    }

    @Override
    // 保存二维码图片到本地目录
    public String saveQrCode(byte[] bytes, String fileName) {
        Path relativePath = Paths.get("qrcode", fileName);
        Path target = resolvePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("二维码保存失败");
        }
        return buildAccessUrl(relativePath);
    }

    // 校验上传图片格式和大小
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > fileProperties.getImageMaxSize()) {
            throw new BusinessException("图片大小超出限制");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)
                || fileProperties.getAllowedImageTypes().stream().noneMatch(item -> item.equalsIgnoreCase(extension))) {
            throw new BusinessException("仅支持 jpg、jpeg、png、webp 图片");
        }
    }

    // 拼接文件保存的绝对路径
    private Path resolvePath(Path relativePath) {
        return Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize().resolve(relativePath);
    }

    // 拼接文件访问 URL
    private String buildAccessUrl(Path relativePath) {
        String prefix = fileProperties.getUploadUrlPrefix();
        String relative = relativePath.toString().replace("\\", "/");
        return prefix + "/" + relative;
    }

    // 获取文件后缀名
    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
