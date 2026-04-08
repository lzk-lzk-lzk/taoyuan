package com.example.peach.modules.variety.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.peach.common.result.PageResult;
import com.example.peach.modules.variety.dto.VarietyAddDTO;
import com.example.peach.modules.variety.dto.VarietyPageQueryDTO;
import com.example.peach.modules.variety.dto.VarietyUpdateDTO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.vo.VarietyDetailVO;
import com.example.peach.modules.variety.vo.VarietyPageVO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public interface FruitVarietyService extends IService<FruitVariety> {

    // 分页查询品种
    PageResult<VarietyPageVO> pageVarieties(VarietyPageQueryDTO dto);

    // 查询品种详情
    VarietyDetailVO getVarietyDetail(Long id);

    // 查询品种实体，不存在时抛异常
    FruitVariety getEntityOrThrow(Long id);

    // 新增品种
    void addVariety(VarietyAddDTO dto);

    // 修改品种
    void updateVariety(VarietyUpdateDTO dto);

    // 删除品种
    void deleteVariety(Long id);

    // 导出品种列表
    ResponseEntity<ByteArrayResource> exportVarieties(VarietyPageQueryDTO dto);
}
