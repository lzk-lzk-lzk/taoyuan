package com.example.peach.modules.qrcode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("qrcode_scan_record")
// 二维码扫码记录实体
public class QrCodeScanRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long varietyId;

    private String varietyName;

    private Long scanUserId;

    private String scanUsername;

    private String scanUserType;

    private String scanIp;

    private LocalDateTime scanTime;
}
