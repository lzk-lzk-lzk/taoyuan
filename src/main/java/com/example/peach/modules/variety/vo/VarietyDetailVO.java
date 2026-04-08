package com.example.peach.modules.variety.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
// 品种详情返回对象
public class VarietyDetailVO {

    private Long id;
    private String varietyCode;
    private String varietyName;
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
    private String coverImage;
    private String distributionArea;
    private String fruitTraits;
    private String cultivationPoints;
    private String qrCodeUrl;
    private String qrTargetUrl;
    private Integer status;
    private Integer sortNum;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
