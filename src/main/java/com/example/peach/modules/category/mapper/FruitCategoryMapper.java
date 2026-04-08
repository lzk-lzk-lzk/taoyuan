package com.example.peach.modules.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.peach.modules.category.entity.FruitCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FruitCategoryMapper extends BaseMapper<FruitCategory> {
    // 种属分类基础 Mapper
}
