package com.example.peach.modules.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
// 新增种属分类参数
public class CategoryAddDTO {

    @NotNull(message = "父级ID不能为空，顶级请传 0")
    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @NotNull(message = "层级不能为空")
    private Integer levelNum;

    private Integer status = 0;

    private Integer sortNum = 0;

    private String remark;
}
