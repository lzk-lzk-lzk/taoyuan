package com.example.peach.modules.variety.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
// 新增品种参数
public class VarietyAddDTO {

    @NotBlank(message = "品种编码不能为空")
    private String varietyCode;

    @NotBlank(message = "品种名称不能为空")
    private String varietyName;

    @NotNull(message = "种属分类不能为空")
    private Long categoryId;

    private String coverImage;

    private String distributionArea;

    private String fruitTraits;

    private String cultivationPoints;

    private String qrTargetUrl;

    private Integer status = 0;

    private Integer sortNum = 0;

    private String remark;
}
