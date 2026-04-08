package com.example.peach.modules.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.modules.category.dto.CategoryAddDTO;
import com.example.peach.modules.category.dto.CategoryUpdateDTO;
import com.example.peach.modules.category.entity.FruitCategory;
import com.example.peach.modules.category.mapper.FruitCategoryMapper;
import com.example.peach.modules.category.service.FruitCategoryService;
import com.example.peach.modules.category.vo.CategoryTreeVO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.mapper.FruitVarietyMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 种属分类业务实现
public class FruitCategoryServiceImpl extends ServiceImpl<FruitCategoryMapper, FruitCategory> implements FruitCategoryService {

    private final FruitVarietyMapper fruitVarietyMapper;

    public FruitCategoryServiceImpl(FruitVarietyMapper fruitVarietyMapper) {
        this.fruitVarietyMapper = fruitVarietyMapper;
    }

    @Override
    // 查询种属分类树
    public List<CategoryTreeVO> treeList() {
        List<FruitCategory> list = lambdaQuery()
                .eq(FruitCategory::getDelFlag, 0)
                .orderByAsc(FruitCategory::getSortNum)
                .orderByAsc(FruitCategory::getCreateTime)
                .list();
        Map<Long, List<FruitCategory>> groupMap = list.stream()
                .collect(Collectors.groupingBy(item -> item.getParentId() == null ? 0L : item.getParentId()));
        return buildChildren(0L, groupMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 新增种属分类
    public void addCategory(CategoryAddDTO dto) {
        validateParent(dto.getParentId(), dto.getLevelNum());
        checkNameUnique(dto.getParentId(), dto.getCategoryName(), null);
        FruitCategory category = new FruitCategory();
        BeanUtils.copyProperties(dto, category);
        category.setDelFlag(0);
        save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改种属分类
    public void updateCategory(CategoryUpdateDTO dto) {
        FruitCategory category = getEntityOrThrow(dto.getId());
        validateParent(dto.getParentId(), dto.getLevelNum());
        checkNameUnique(dto.getParentId(), dto.getCategoryName(), dto.getId());
        BeanUtils.copyProperties(dto, category);
        updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 删除种属分类
    public void deleteCategory(Long id) {
        FruitCategory category = getEntityOrThrow(id);
        long childCount = lambdaQuery().eq(FruitCategory::getParentId, id).eq(FruitCategory::getDelFlag, 0).count();
        if (childCount > 0) {
            throw new BusinessException("请先删除下级分类");
        }
        long varietyCount = fruitVarietyMapper.selectCount(new LambdaQueryWrapper<FruitVariety>()
                .eq(FruitVariety::getCategoryId, category.getId())
                .eq(FruitVariety::getDelFlag, 0));
        if (varietyCount > 0) {
            throw new BusinessException("该分类已被品种使用，不能删除");
        }
        removeById(id);
    }

    @Override
    // 查询分类，不存在时抛异常
    public FruitCategory getEntityOrThrow(Long id) {
        FruitCategory category = getById(id);
        if (category == null || Objects.equals(category.getDelFlag(), 1)) {
            throw new BusinessException("种属分类不存在");
        }
        return category;
    }

    @Override
    // 生成分类完整路径
    public String buildCategoryPath(Long categoryId) {
        if (categoryId == null || categoryId == 0L) {
            return null;
        }
        List<String> names = new ArrayList<>();
        FruitCategory current = getEntityOrThrow(categoryId);
        names.add(current.getCategoryName());
        while (current.getParentId() != null && current.getParentId() != 0L) {
            current = getEntityOrThrow(current.getParentId());
            names.add(current.getCategoryName());
        }
        java.util.Collections.reverse(names);
        return String.join(" / ", names);
    }

    private List<CategoryTreeVO> buildChildren(Long parentId, Map<Long, List<FruitCategory>> groupMap) {
        return groupMap.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(FruitCategory::getSortNum, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(FruitCategory::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(item -> {
                    CategoryTreeVO vo = new CategoryTreeVO();
                    BeanUtils.copyProperties(item, vo);
                    vo.setChildren(buildChildren(item.getId(), groupMap));
                    return vo;
                })
                .toList();
    }

    private void validateParent(Long parentId, Integer levelNum) {
        if (levelNum == null || levelNum < 1) {
            throw new BusinessException("层级必须大于 0");
        }
        if (parentId == null || parentId == 0L) {
            if (levelNum != 1) {
                throw new BusinessException("顶级分类层级必须为 1");
            }
            return;
        }
        FruitCategory parent = getEntityOrThrow(parentId);
        if (!Objects.equals(parent.getLevelNum() + 1, levelNum)) {
            throw new BusinessException("分类层级与父级不匹配");
        }
    }

    private void checkNameUnique(Long parentId, String categoryName, Long excludeId) {
        Long count = lambdaQuery()
                .eq(FruitCategory::getParentId, parentId)
                .eq(FruitCategory::getCategoryName, categoryName)
                .eq(FruitCategory::getDelFlag, 0)
                .ne(excludeId != null, FruitCategory::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
    }
}
