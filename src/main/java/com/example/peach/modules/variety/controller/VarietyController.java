package com.example.peach.modules.variety.controller;

import com.example.peach.common.result.PageResult;
import com.example.peach.common.result.Result;
import com.example.peach.modules.variety.dto.VarietyAddDTO;
import com.example.peach.modules.variety.dto.VarietyPageQueryDTO;
import com.example.peach.modules.variety.dto.VarietyUpdateDTO;
import com.example.peach.modules.variety.service.FruitVarietyService;
import com.example.peach.modules.variety.vo.VarietyDetailVO;
import com.example.peach.modules.variety.vo.VarietyPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/varieties")
@Tag(name = "品种管理")
// 品种管理接口
public class VarietyController {

    private final FruitVarietyService fruitVarietyService;

    public VarietyController(FruitVarietyService fruitVarietyService) {
        this.fruitVarietyService = fruitVarietyService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询品种")
    public Result<PageResult<VarietyPageVO>> page(VarietyPageQueryDTO dto) {
        return Result.success(fruitVarietyService.pageVarieties(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询品种详情")
    public Result<VarietyDetailVO> detail(@PathVariable Long id) {
        return Result.success(fruitVarietyService.getVarietyDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "新增品种")
    public Result<Void> add(@Valid @RequestBody VarietyAddDTO dto) {
        fruitVarietyService.addVariety(dto);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "修改品种")
    public Result<Void> update(@Valid @RequestBody VarietyUpdateDTO dto) {
        fruitVarietyService.updateVariety(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "删除品种")
    public Result<Void> delete(@PathVariable Long id) {
        fruitVarietyService.deleteVariety(id);
        return Result.success();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "导出品种列表")
    public ResponseEntity<ByteArrayResource> export(VarietyPageQueryDTO dto) {
        return fruitVarietyService.exportVarieties(dto);
    }
}
