package com.example.peach.modules.qrcode.dto;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
// 扫码记录分页查询参数
public class QrCodeScanPageQueryDTO {

    private Long pageNum = 1L;

    private Long pageSize = 10L;

    private String varietyName;

    private String scanUserType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
