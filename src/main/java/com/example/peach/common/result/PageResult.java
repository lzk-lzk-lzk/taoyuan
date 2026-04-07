package com.example.peach.common.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 统一分页返回对象
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;
}
