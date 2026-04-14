package com.example.peach.modules.qrcode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.peach.common.context.LoginUser;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.result.PageResult;
import com.example.peach.common.utils.SecurityUtils;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.qrcode.dto.QrCodeExportDTO;
import com.example.peach.modules.qrcode.dto.QrCodeScanPageQueryDTO;
import com.example.peach.modules.qrcode.entity.QrCodeScanRecord;
import com.example.peach.modules.qrcode.mapper.QrCodeScanRecordMapper;
import com.example.peach.modules.qrcode.service.QrCodeService;
import com.example.peach.modules.qrcode.support.QrCodeCardGenerator;
import com.example.peach.modules.qrcode.vo.QrCodeInfoVO;
import com.example.peach.modules.qrcode.vo.QrCodeScanRecordVO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.service.FruitVarietyService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.BeanUtils;
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
    private final QrCodeScanRecordMapper qrCodeScanRecordMapper;
    private final QrCodeCardGenerator qrCodeCardGenerator;

    public QrCodeServiceImpl(FruitVarietyService fruitVarietyService,
                             FileStorageService fileStorageService,
                             QrCodeScanRecordMapper qrCodeScanRecordMapper,
                             QrCodeCardGenerator qrCodeCardGenerator) {
        this.fruitVarietyService = fruitVarietyService;
        this.fileStorageService = fileStorageService;
        this.qrCodeScanRecordMapper = qrCodeScanRecordMapper;
        this.qrCodeCardGenerator = qrCodeCardGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 生成并保存单个品种二维码卡片
    public QrCodeInfoVO generate(Long id) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        refreshQrCode(variety);
        return toInfoVo(variety);
    }

    @Override
    // 查询单个品种二维码信息
    public QrCodeInfoVO getInfo(Long id) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        return toInfoVo(variety);
    }

    @Override
    // 批量获取二维码图片下载信息
    public List<QrCodeInfoVO> listDownloadInfos(QrCodeExportDTO dto) {
        return dto.getIds().stream()
                .distinct()
                .map(fruitVarietyService::getEntityOrThrow)
                .map(this::ensureQrCodeReady)
                .map(this::toInfoVo)
                .toList();
    }

    @Override
    // 批量导出二维码压缩包
    public ResponseEntity<ByteArrayResource> exportZip(QrCodeExportDTO dto) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            List<Long> ids = dto.getIds().stream().distinct().toList();
            for (Long id : ids) {
                FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
                QrCodeCardGenerator.GenerateResult result = qrCodeCardGenerator.generate(variety);
                zos.putNextEntry(new ZipEntry(variety.getVarietyName() + "_" + id + ".png"));
                zos.write(result.imageBytes());
                zos.closeEntry();
            }
            zos.finish();
            String fileName = "qrcode_export_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".zip";
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 记录扫码
    public void recordScan(Long id, String scanIp) {
        FruitVariety variety = fruitVarietyService.getEntityOrThrow(id);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        QrCodeScanRecord record = new QrCodeScanRecord();
        record.setVarietyId(variety.getId());
        record.setVarietyName(variety.getVarietyName());
        record.setScanUserId(loginUser.getUserId());
        record.setScanUsername(loginUser.getUsername());
        record.setScanUserType(loginUser.getUserType());
        record.setScanIp(scanIp);
        record.setScanTime(LocalDateTime.now());
        qrCodeScanRecordMapper.insert(record);
    }

    @Override
    // 分页查询扫码记录
    public PageResult<QrCodeScanRecordVO> pageScanRecords(QrCodeScanPageQueryDTO dto) {
        Page<QrCodeScanRecord> page = qrCodeScanRecordMapper.selectPage(new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<QrCodeScanRecord>()
                        .like(StringUtils.hasText(dto.getVarietyName()), QrCodeScanRecord::getVarietyName, dto.getVarietyName())
                        .eq(StringUtils.hasText(dto.getScanUserType()), QrCodeScanRecord::getScanUserType, dto.getScanUserType())
                        .ge(dto.getStartTime() != null, QrCodeScanRecord::getScanTime, dto.getStartTime())
                        .le(dto.getEndTime() != null, QrCodeScanRecord::getScanTime, dto.getEndTime())
                        .orderByDesc(QrCodeScanRecord::getScanTime));
        List<QrCodeScanRecordVO> records = page.getRecords().stream().map(this::toScanVo).toList();
        return new PageResult<>(records, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    // 刷新二维码并保存地址
    private void refreshQrCode(FruitVariety variety) {
        QrCodeCardGenerator.GenerateResult result = qrCodeCardGenerator.generate(variety);
        String fileName = "variety_" + variety.getId() + ".png";
        String qrCodeUrl = fileStorageService.saveQrCode(result.imageBytes(), fileName);
        variety.setQrTargetUrl(result.targetUrl());
        variety.setQrCodeUrl(qrCodeUrl);
        fruitVarietyService.updateById(variety);
    }

    // 确保品种已有可下载的二维码图片
    private FruitVariety ensureQrCodeReady(FruitVariety variety) {
        if (!StringUtils.hasText(variety.getQrCodeUrl())) {
            refreshQrCode(variety);
        }
        return variety;
    }

    // 转换二维码信息返回对象
    private QrCodeInfoVO toInfoVo(FruitVariety variety) {
        QrCodeInfoVO vo = new QrCodeInfoVO();
        vo.setVarietyId(variety.getId());
        vo.setVarietyName(variety.getVarietyName());
        vo.setQrCodeUrl(variety.getQrCodeUrl());
        vo.setQrTargetUrl(variety.getQrTargetUrl());
        return vo;
    }

    // 转换扫码记录返回对象
    private QrCodeScanRecordVO toScanVo(QrCodeScanRecord record) {
        QrCodeScanRecordVO vo = new QrCodeScanRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
