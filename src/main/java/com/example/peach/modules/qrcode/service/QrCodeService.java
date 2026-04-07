package com.example.peach.modules.qrcode.service;

import com.example.peach.modules.qrcode.dto.QrCodeExportDTO;
import com.example.peach.modules.qrcode.vo.QrCodeInfoVO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public interface QrCodeService {

    // 生成单个品种二维码

    QrCodeInfoVO generate(Long id);

    // 查询二维码信息
    QrCodeInfoVO getInfo(Long id);

    // 批量导出二维码 ZIP
    ResponseEntity<ByteArrayResource> exportZip(QrCodeExportDTO dto);
}
