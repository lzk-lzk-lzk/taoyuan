package com.example.peach.modules.qrcode.controller;

import com.example.peach.common.result.Result;
import com.example.peach.modules.qrcode.dto.QrCodeExportDTO;
import com.example.peach.modules.qrcode.service.QrCodeService;
import com.example.peach.modules.qrcode.vo.QrCodeInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qrcode")
@Tag(name = "二维码管理")
// 二维码管理接口
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generate/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "生成单个品种二维码")
    // 生成单个品种二维码
    public Result<QrCodeInfoVO> generate(@PathVariable Long id) {
        return Result.success(qrCodeService.generate(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询二维码信息")
    // 查询品种二维码信息
    public Result<QrCodeInfoVO> info(@PathVariable Long id) {
        return Result.success(qrCodeService.getInfo(id));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "批量导出二维码ZIP")
    // 批量导出二维码 ZIP
    public ResponseEntity<ByteArrayResource> export(@Valid @RequestBody QrCodeExportDTO dto) {
        return qrCodeService.exportZip(dto);
    }
}
