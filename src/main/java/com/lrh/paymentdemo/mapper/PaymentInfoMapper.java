package com.lrh.paymentdemo.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lrh.paymentdemo.entity.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {
}
