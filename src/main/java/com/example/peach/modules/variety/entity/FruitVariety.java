package com.example.peach.modules.variety.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("fruit_variety")
// 品种信息实体
public class FruitVariety {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String varietyCode;

    private String varietyName;

    private String categoryName;

    private String coverImage;

    private String distributionArea;

    private String fruitTraits;

    private String cultivationPoints;

    private String qrCodeUrl;

    private String qrTargetUrl;

    private Integer status;

    private Integer sortNum;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer delFlag;
}
