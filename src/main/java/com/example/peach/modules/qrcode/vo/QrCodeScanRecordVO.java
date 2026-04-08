package com.example.peach.modules.qrcode.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
// 扫码记录返回对象
public class QrCodeScanRecordVO {

    private Long id;

    private Long varietyId;

    private String varietyName;

    private Long scanUserId;

    private String scanUsername;

    private String scanUserType;

    private String scanIp;

    private LocalDateTime scanTime;
}
