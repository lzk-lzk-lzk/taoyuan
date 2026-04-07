package com.example.peach.modules.qrcode.vo;

import lombok.Data;

@Data
// 二维码信息返回对象
public class QrCodeInfoVO {

    private Long varietyId;
    private String varietyName;
    private String qrCodeUrl;
    private String qrTargetUrl;
}
