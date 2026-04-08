package com.example.peach.modules.variety.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
// 品种分页列表返回对象
public class VarietyPageVO {

    private Long id;
    private String varietyCode;
    private String varietyName;
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    private String coverImage;
    private String distributionArea;
    private String qrCodeUrl;
    private Integer status;
    private Integer sortNum;
    private String remark;
    private LocalDateTime createTime;
}
