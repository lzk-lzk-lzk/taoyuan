package com.example.peach.modules.qrcode.service.impl;

import cn.hutool.core.util.IdUtil;
import com.example.peach.common.config.QrCodeProperties;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.qrcode.dto.QrCodeExportDTO;
import com.example.peach.modules.qrcode.service.QrCodeService;
import com.example.peach.modules.qrcode.vo.QrCodeInfoVO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.service.FruitVarietyService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 二维码业务实现
public class QrCodeServiceImpl implements QrCodeService {

    private final FruitVarietyService fruitVarietyService;
    private final FileStorageService fileStorageService;
    private final QrCodeProperties qrCodeProperties;

    public QrCodeServiceImpl(FruitVarietyService fruitVarietyService,
                             FileStorageService fileStorageService,
                             QrCodeProperties qrCodeProperties) {
        this.fruitVarietyService = fruitVarietyService;
        this.fileStorageService = fileStorageService;
        this.qrCodeProperties = qrCodeProperties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 生成并保存单个品种二维码
    public QrCodeInfoVO generate(Long id) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        String targetUrl = resolveTargetUrl(variety);
        byte[] bytes = createQrCode(targetUrl);
        String fileName = "variety_" + id + "_" + IdUtil.fastSimpleUUID() + ".png";
        String qrCodeUrl = fileStorageService.saveQrCode(bytes, fileName);
        variety.setQrTargetUrl(targetUrl);
        variety.setQrCodeUrl(qrCodeUrl);
        fruitVarietyService.updateById(variety);
        return toInfoVo(variety);
    }

    @Override
    // 查询单个品种二维码信息
    public QrCodeInfoVO getInfo(Long id) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        return toInfoVo(variety);
    }

    @Override
    // 批量导出二维码压缩包
    public ResponseEntity<ByteArrayResource> exportZip(QrCodeExportDTO dto) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            List<Long> ids = dto.getIds().stream().distinct().toList();
            for (Long id : ids) {
                QrCodeInfoVO info = getOrGenerateInfo(id);
                FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
                byte[] bytes = createQrCode(info.getQrTargetUrl());
                zos.putNextEntry(new ZipEntry(variety.getVarietyName() + "_" + id + ".png"));
                zos.write(bytes);
                zos.closeEntry();
            }
            zos.finish();
            String fileName = "qrcode_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".zip";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            ByteArrayResource resource = new ByteArrayResource(bos.toByteArray());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            throw new BusinessException("二维码导出失败");
        }
    }

    // 如果二维码不存在则先生成
    private QrCodeInfoVO getOrGenerateInfo(Long id) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        if (!StringUtils.hasText(variety.getQrCodeUrl()) || !StringUtils.hasText(variety.getQrTargetUrl())) {
            return generate(id);
        }
        return toInfoVo(variety);
    }

    // 计算二维码实际跳转地址
    private String resolveTargetUrl(FruitVariety variety) {
        if (StringUtils.hasText(variety.getQrTargetUrl())) {
            return variety.getQrTargetUrl();
        }
        return qrCodeProperties.getDefaultTargetPrefix() + variety.getId();
    }

    // 生成二维码图片字节流
    private byte[] createQrCode(String content) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 320, 320, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("生成二维码失败");
        }
    }

    // 转换二维码返回对象
    private QrCodeInfoVO toInfoVo(FruitVariety variety) {
        QrCodeInfoVO vo = new QrCodeInfoVO();
        vo.setVarietyId(variety.getId());
        vo.setVarietyName(variety.getVarietyName());
        vo.setQrCodeUrl(variety.getQrCodeUrl());
        vo.setQrTargetUrl(resolveTargetUrl(variety));
        return vo;
    }
}
