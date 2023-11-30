package com.lrh.paymentdemo.service;

import com.alipay.v3.model.AlipayTradeQueryModel;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lrh.paymentdemo.entity.PaymentInfo;
import com.wechat.pay.java.service.payments.model.Transaction;

import java.util.Map;

public interface PaymentInfoService extends IService<PaymentInfo> {

    void createPaymentInfo(Transaction transaction);

    void createPaymentInfo(Map<String,String> params);

    void createPaymentInfo(AlipayTradeQueryResponseModel alipayTradeQueryResponseModel);
}
