package com.example.peach.modules.variety.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.result.PageResult;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.category.entity.FruitCategory;
import com.example.peach.modules.category.service.FruitCategoryService;
import com.example.peach.modules.file.service.FileStorageService;
import com.example.peach.modules.qrcode.support.QrCodeCardGenerator;
import com.example.peach.modules.variety.dto.VarietyAddDTO;
import com.example.peach.modules.variety.dto.VarietyPageQueryDTO;
import com.example.peach.modules.variety.dto.VarietyUpdateDTO;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.example.peach.modules.variety.mapper.FruitVarietyMapper;
import com.example.peach.modules.variety.service.FruitVarietyService;
import com.example.peach.modules.variety.vo.VarietyDetailVO;
import com.example.peach.modules.variety.vo.VarietyPageVO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// 品种业务实现
public class FruitVarietyServiceImpl extends ServiceImpl<FruitVarietyMapper, FruitVariety> implements FruitVarietyService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FruitCategoryService fruitCategoryService;
    private final QrCodeCardGenerator qrCodeCardGenerator;
    private final FileStorageService fileStorageService;

    public FruitVarietyServiceImpl(FruitCategoryService fruitCategoryService,
                                   QrCodeCardGenerator qrCodeCardGenerator,
                                   FileStorageService fileStorageService) {
        this.fruitCategoryService = fruitCategoryService;
        this.qrCodeCardGenerator = qrCodeCardGenerator;
        this.fileStorageService = fileStorageService;
    }

    @Override
    // 分页查询品种列表
    public PageResult<VarietyPageVO> pageVarieties(VarietyPageQueryDTO dto) {
        Page<FruitVariety> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), buildQueryWrapper(dto));
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
    // 新增品种并自动生成二维码卡片
    public void addVariety(VarietyAddDTO dto) {
        FruitVariety variety = new FruitVariety();
        BeanUtils.copyProperties(dto, variety);
        fillCategoryInfo(variety, dto.getCategoryId());
        variety.setDistributionArea(normalizeDistributionArea(dto.getDistributionArea()));
        variety.setVarietyCode(generateVarietyCode(variety.getDistributionArea(), null));
        variety.setDelFlag(0);
        save(variety);
        refreshQrCodeCard(variety);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 修改品种并同步刷新二维码卡片
    public void updateVariety(VarietyUpdateDTO dto) {
        FruitVariety variety = getEntityOrThrow(dto.getId());
        BeanUtils.copyProperties(dto, variety);
        fillCategoryInfo(variety, dto.getCategoryId());
        variety.setDistributionArea(normalizeDistributionArea(dto.getDistributionArea()));
        variety.setVarietyCode(generateVarietyCode(variety.getDistributionArea(), dto.getId()));
        updateById(variety);
        refreshQrCodeCard(variety);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // 逻辑删除品种
    public void deleteVariety(Long id) {
        getEntityOrThrow(id);
        removeById(id);
    }

    @Override
    // 导出品种列表
    public ResponseEntity<ByteArrayResource> exportVarieties(VarietyPageQueryDTO dto) {
        List<FruitVariety> list = list(buildQueryWrapper(dto));
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append("品种编码,品种名称,种属路径,末级分类,分布地区,状态,排序,创建时间\n");
        for (FruitVariety item : list) {
            builder.append(csv(item.getVarietyCode())).append(',')
                    .append(csv(item.getVarietyName())).append(',')
                    .append(csv(item.getCategoryPath())).append(',')
                    .append(csv(item.getCategoryName())).append(',')
                    .append(csv(item.getDistributionArea())).append(',')
                    .append(csv(item.getStatus() != null && item.getStatus() == 0 ? "正常" : "停用")).append(',')
                    .append(csv(item.getSortNum())).append(',')
                    .append(csv(formatDateTime(item.getCreateTime()))).append('\n');
        }
        String fileName = URLEncoder.encode("variety_export.csv", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        ByteArrayResource resource = new ByteArrayResource(builder.toString().getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    private LambdaQueryWrapper<FruitVariety> buildQueryWrapper(VarietyPageQueryDTO dto) {
        return new LambdaQueryWrapper<FruitVariety>()
                .like(StringUtils.hasText(dto.getVarietyName()), FruitVariety::getVarietyName, dto.getVarietyName())
                .and(StringUtils.hasText(dto.getCategoryName()), wrapper -> wrapper
                        .like(FruitVariety::getCategoryName, dto.getCategoryName())
                        .or()
                        .like(FruitVariety::getCategoryPath, dto.getCategoryName()))
                .like(StringUtils.hasText(dto.getDistributionArea()), FruitVariety::getDistributionArea, dto.getDistributionArea())
                .ge(dto.getStartTime() != null, FruitVariety::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, FruitVariety::getCreateTime, dto.getEndTime())
                .eq(FruitVariety::getDelFlag, 0)
                .orderByDesc(FruitVariety::getCreateTime);
    }

    // 转换分页返回对象
    private VarietyPageVO toPageVo(FruitVariety variety) {
        VarietyPageVO vo = new VarietyPageVO();
        BeanUtils.copyProperties(variety, vo);
        return vo;
    }

    private void fillCategoryInfo(FruitVariety variety, Long categoryId) {
        FruitCategory category = fruitCategoryService.getEntityOrThrow(categoryId);
        variety.setCategoryId(category.getId());
        variety.setCategoryName(category.getCategoryName());
        variety.setCategoryPath(fruitCategoryService.buildCategoryPath(categoryId));
    }

    private void refreshQrCodeCard(FruitVariety variety) {
        QrCodeCardGenerator.GenerateResult result = qrCodeCardGenerator.generate(variety);
        String fileName = "variety_" + variety.getId() + ".png";
        String qrCodeUrl = fileStorageService.saveQrCode(result.imageBytes(), fileName);
        variety.setQrTargetUrl(result.targetUrl());
        variety.setQrCodeUrl(qrCodeUrl);
        updateById(variety);
    }

    // 按年份-分布地区-三位序号生成品种编码
    private String generateVarietyCode(String distributionArea, Long excludeId) {
        String area = normalizeDistributionArea(distributionArea);
        String prefix = LocalDate.now().getYear() + "-" + area + "-";
        List<FruitVariety> varieties = lambdaQuery()
                .likeRight(FruitVariety::getVarietyCode, prefix)
                .eq(FruitVariety::getDelFlag, 0)
                .ne(excludeId != null, FruitVariety::getId, excludeId)
                .list();
        int nextNumber = varieties.stream()
                .map(FruitVariety::getVarietyCode)
                .map(code -> extractCodeNumber(code, prefix))
                .filter(number -> number > 0)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
        return prefix + String.format("%03d", nextNumber);
    }

    // 规范化分布地区，避免编码里混入首尾空格
    private String normalizeDistributionArea(String distributionArea) {
        if (!StringUtils.hasText(distributionArea)) {
            throw new BusinessException("分布地区不能为空");
        }
        return distributionArea.trim();
    }

    // 解析编码末尾的顺序号
    private int extractCodeNumber(String varietyCode, String prefix) {
        if (!StringUtils.hasText(varietyCode) || !varietyCode.startsWith(prefix)) {
            return 0;
        }
        String suffix = varietyCode.substring(prefix.length());
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }
}
