package com.example.peach.modules.qrcode.service;

import com.example.peach.common.result.PageResult;
import com.example.peach.modules.qrcode.dto.QrCodeExportDTO;
import com.example.peach.modules.qrcode.dto.QrCodeScanPageQueryDTO;
import com.example.peach.modules.qrcode.vo.QrCodeInfoVO;
import com.example.peach.modules.qrcode.vo.QrCodeScanRecordVO;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public interface QrCodeService {

    // 生成单个品种二维码
    QrCodeInfoVO generate(Long id);

    // 查询二维码信息
    QrCodeInfoVO getInfo(Long id);

    // 批量获取二维码图片下载信息
    List<QrCodeInfoVO> listDownloadInfos(QrCodeExportDTO dto);

    // 批量导出二维码 ZIP
    ResponseEntity<ByteArrayResource> exportZip(QrCodeExportDTO dto);

    // 记录扫码
    void recordScan(Long id, String scanIp);

    // 分页查询扫码记录
    PageResult<QrCodeScanRecordVO> pageScanRecords(QrCodeScanPageQueryDTO dto);
}
