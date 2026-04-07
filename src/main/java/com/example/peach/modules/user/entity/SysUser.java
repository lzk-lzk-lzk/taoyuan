package com.example.peach.modules.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_user")
// 系统用户实体
public class SysUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String nickName;

    private String phone;

    private String avatar;

    private String openId;

    private Integer status;

    private String userType;

    private LocalDateTime lastLoginTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer delFlag;
}
