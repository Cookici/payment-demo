package com.lrh.paymentdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrh.paymentdemo.entity.PaymentInfo;
import com.wechat.pay.java.service.payments.model.Transaction;

public interface PaymentInfoService extends IService<PaymentInfo> {

    void createPaymentInfo(Transaction transaction);
}
