package com.example.peach.modules.user.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
// 用户分页列表返回对象
public class UserPageVO {

    private Long id;
    private String username;
    private String nickName;
    private String phone;
    private String avatar;
    private Integer status;
    private String userType;
    private LocalDateTime lastLoginTime;
    private String remark;
    private LocalDateTime createTime;
}
