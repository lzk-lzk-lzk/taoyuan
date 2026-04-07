package com.example.peach.modules.variety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.result.PageResult;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.variety.dto.VarietyAddDTO;
import com.example.peach.modules.variety.dto.VarietyPageQueryDTO;
import com.example.peach.modules.variety.dto.VarietyUpdateDTO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.mapper.FruitVarietyMapper;
import com.example.peach.modules.variety.service.FruitVarietyService;
import com.example.peach.modules.variety.vo.VarietyDetailVO;
import com.example.peach.modules.variety.vo.VarietyPageVO;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 品种业务实现
public class FruitVarietyServiceImpl extends ServiceImpl<FruitVarietyMapper, FruitVariety> implements FruitVarietyService {

    @Override
    // 分页查询品种列表
    public PageResult<VarietyPageVO> pageVarieties(VarietyPageQueryDTO dto) {
        Page<FruitVariety> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()),
                new LambdaQueryWrapper<FruitVariety>()
                        .like(StringUtils.hasText(dto.getVarietyName()), FruitVariety::getVarietyName, dto.getVarietyName())
                        .like(StringUtils.hasText(dto.getCategoryName()), FruitVariety::getCategoryName, dto.getCategoryName())
                        .like(StringUtils.hasText(dto.getDistributionArea()), FruitVariety::getDistributionArea, dto.getDistributionArea())
                        .ge(dto.getStartTime() != null, FruitVariety::getCreateTime, dto.getStartTime())
                        .le(dto.getEndTime() != null, FruitVariety::getCreateTime, dto.getEndTime())
                        .eq(FruitVariety::getDelFlag, 0)
                        .orderByDesc(FruitVariety::getCreateTime));
        List<VarietyPageVO> records = page.getRecords().stream().map(this::toPageVo).toList();
        return new PageResult<>(records, page.getTotal(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    // 查询品种详情
    public VarietyDetailVO getVarietyDetail(Long id) {
        FruitVariety variety = getEntityOrThrow(id);
        VarietyDetailVO vo = new VarietyDetailVO();
        BeanUtils.copyProperties(variety, vo);
        return vo;
    }

    @Override
    // 查询品种实体，不存在时抛异常
    public FruitVariety getEntityOrThrow(Long id) {
        FruitVariety variety = getById(id);
        if (variety == null || Integer.valueOf(1).equals(variety.getDelFlag())) {
            throw new BusinessException("品种不存在");
        }
        return variety;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 新增品种并校验编码唯一
    public void addVariety(VarietyAddDTO dto) {
        checkVarietyCodeUnique(dto.getVarietyCode(), null);
        FruitVariety variety = new FruitVariety();
        BeanUtils.copyProperties(dto, variety);
        variety.setDelFlag(0);
        save(variety);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改品种并校验编码唯一
    public void updateVariety(VarietyUpdateDTO dto) {
        checkVarietyCodeUnique(dto.getVarietyCode(), dto.getId());
        FruitVariety variety = getEntityOrThrow(dto.getId());
        BeanUtils.copyProperties(dto, variety);
        updateById(variety);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 逻辑删除品种
    public void deleteVariety(Long id) {
        getEntityOrThrow(id);
        removeById(id);
    }

    // 校验品种编码是否重复
    private void checkVarietyCodeUnique(String varietyCode, Long excludeId) {
        Long count = lambdaQuery()
                .eq(FruitVariety::getVarietyCode, varietyCode)
                .eq(FruitVariety::getDelFlag, 0)
                .ne(excludeId != null, FruitVariety::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("品种编码已存在");
        }
    }

    // 转换分页返回对象
    private VarietyPageVO toPageVo(FruitVariety variety) {
        VarietyPageVO vo = new VarietyPageVO();
        BeanUtils.copyProperties(variety, vo);
        return vo;
    }
}
