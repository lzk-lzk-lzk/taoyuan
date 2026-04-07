package com.example.peach.modules.variety.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
// 品种分页查询参数
public class VarietyPageQueryDTO {

    @Min(value = 1, message = "pageNum 最小为 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize 最小为 1")
    private Long pageSize = 10L;

    private String varietyName;

    private String categoryName;

    private String distributionArea;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
