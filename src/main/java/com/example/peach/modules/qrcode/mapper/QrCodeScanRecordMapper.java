package com.example.peach.modules.qrcode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.peach.modules.qrcode.entity.QrCodeScanRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QrCodeScanRecordMapper extends BaseMapper<QrCodeScanRecord> {
    // 扫码记录基础 Mapper
}
