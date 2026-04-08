package com.example.peach.modules.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.peach.modules.category.dto.CategoryAddDTO;
import com.example.peach.modules.category.dto.CategoryUpdateDTO;
import com.example.peach.modules.category.entity.FruitCategory;
import com.example.peach.modules.category.vo.CategoryTreeVO;
import java.util.List;

public interface FruitCategoryService extends IService<FruitCategory> {

    // 查询种属分类树
    List<CategoryTreeVO> treeList();

    // 新增种属分类
    void addCategory(CategoryAddDTO dto);

    // 修改种属分类
    void updateCategory(CategoryUpdateDTO dto);

    // 删除种属分类
    void deleteCategory(Long id);

    // 查询分类，不存在时抛异常
    FruitCategory getEntityOrThrow(Long id);

    // 生成分类完整路径
    String buildCategoryPath(Long categoryId);
}
