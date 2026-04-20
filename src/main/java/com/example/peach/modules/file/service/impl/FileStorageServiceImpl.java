package com.example.peach.modules.file.service.impl;

import com.example.peach.common.config.FileProperties;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.file.vo.FileUploadVO;
import java.io.ByteArrayInputStream;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
// 文件存储实现，支持本地和 RustFS
public class FileStorageServiceImpl implements FileStorageService {

    private final FileProperties fileProperties;
    private final S3Client s3Client;

    public FileStorageServiceImpl(FileProperties fileProperties, S3Client s3Client) {
        this.fileProperties = fileProperties;
        this.s3Client = s3Client;
    }

    @Override
    // 上传图片
    public FileUploadVO uploadImage(MultipartFile file) {
        validateImage(file);
        String extension = getExtension(file.getOriginalFilename());
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String objectKey = "images/" + dateDir + "/" + fileName;
        String url;
        try {
            url = saveBytes(file.getBytes(), objectKey, file.getContentType());
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }
        return new FileUploadVO(fileName, url);
    }

    @Override
    // 保存二维码图片
    public String saveQrCode(byte[] bytes, String fileName) {
        String objectKey = "qrcode/" + fileName;
        return saveBytes(bytes, objectKey, "image/png");
    }

    private String saveBytes(byte[] bytes, String objectKey, String contentType) {
        if ("rustfs".equalsIgnoreCase(fileProperties.getStorageType())) {
            return saveToRustFs(bytes, objectKey, contentType);
        }
        return saveToLocal(bytes, objectKey);
    }

    private String saveToRustFs(byte[] bytes, String objectKey, String contentType) {
        try {
            ensureBucketExists();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(fileProperties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(new ByteArrayInputStream(bytes), bytes.length));
            return buildRustFsUrl(objectKey);
        } catch (Exception e) {
            throw new BusinessException("RustFS 文件保存失败：" + e.getMessage());
        }
    }

    private String saveToLocal(byte[] bytes, String objectKey) {
        Path relativePath = Paths.get("", objectKey.split("/"));
        Path target = resolvePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("本地文件保存失败");
        }
        return buildLocalAccessUrl(relativePath);
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(fileProperties.getBucket()).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(fileProperties.getBucket()).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(fileProperties.getBucket()).build());
            } else {
                throw e;
            }
        }
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

    private Path resolvePath(Path relativePath) {
        return Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize().resolve(relativePath);
    }

    private String buildLocalAccessUrl(Path relativePath) {
        String prefix = fileProperties.getUploadUrlPrefix();
        String relative = relativePath.toString().replace("\\", "/");
        String url = prefix + "/" + relative;
        if (StringUtils.hasText(fileProperties.getPublicUrlPrefix())) {
            return trimTrailingSlash(fileProperties.getPublicUrlPrefix()) + url;
        }
        return url;
    }

    private String buildRustFsUrl(String objectKey) {
        String endpoint = fileProperties.getEndpoint();
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            endpoint = "http://" + endpoint;
        }
        return endpoint + "/" + fileProperties.getBucket() + "/" + objectKey;
    }

    // 获取文件后缀名
    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    // 去掉末尾斜杠，避免拼接出双斜杠
    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
