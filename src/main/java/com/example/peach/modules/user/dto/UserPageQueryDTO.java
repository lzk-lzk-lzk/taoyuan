package com.example.peach.modules.user.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
// 用户分页查询参数
public class UserPageQueryDTO {

    @Min(value = 1, message = "pageNum 最小为 1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize 最小为 1")
    private Long pageSize = 10L;

    private String username;

    private String nickName;

    private String phone;

    private Integer status;

    private String userType;
}
