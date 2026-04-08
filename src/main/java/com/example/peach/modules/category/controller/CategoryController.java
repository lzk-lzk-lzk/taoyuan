package com.example.peach.modules.category.controller;

import com.example.peach.common.result.Result;
import com.example.peach.modules.category.dto.CategoryAddDTO;
import com.example.peach.modules.category.dto.CategoryUpdateDTO;
import com.example.peach.modules.category.service.FruitCategoryService;
import com.example.peach.modules.category.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/categories")
@Tag(name = "种属分类")
// 种属分类接口
public class CategoryController {

    private final FruitCategoryService fruitCategoryService;

    public CategoryController(FruitCategoryService fruitCategoryService) {
        this.fruitCategoryService = fruitCategoryService;
    }

    @GetMapping("/tree")
    @Operation(summary = "查询种属分类树")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(fruitCategoryService.treeList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "新增种属分类")
    public Result<Void> add(@Valid @RequestBody CategoryAddDTO dto) {
        fruitCategoryService.addCategory(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "修改种属分类")
    public Result<Void> update(@Valid @RequestBody CategoryUpdateDTO dto) {
        fruitCategoryService.updateCategory(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "删除种属分类")
    public Result<Void> delete(@PathVariable Long id) {
        fruitCategoryService.deleteCategory(id);
        return Result.success();
    }
}
