package com.example.peach.modules.qrcode.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
// 二维码批量导出参数
public class QrCodeExportDTO {

    @NotEmpty(message = "导出ID列表不能为空")
    private List<Long> ids;
}
