package com.example.peach.modules.variety.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
// 修改品种参数
public class VarietyUpdateDTO {

    @NotNull(message = "品种ID不能为空")
    private Long id;

    @NotBlank(message = "品种编码不能为空")
    private String varietyCode;

    @NotBlank(message = "品种名称不能为空")
    private String varietyName;

    @NotBlank(message = "类别不能为空")
    private String categoryName;

    private String coverImage;

    private String distributionArea;

    private String fruitTraits;

    private String cultivationPoints;

    private String qrTargetUrl;

    private Integer status;

    private Integer sortNum;

    private String remark;
}
