package com.example.peach.modules.category.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
// 种属分类树节点
public class CategoryTreeVO {

    private Long id;

    private Long parentId;

    private String categoryName;

    private Integer levelNum;

    private Integer status;

    private Integer sortNum;

    private String remark;

    private List<CategoryTreeVO> children = new ArrayList<>();
}
