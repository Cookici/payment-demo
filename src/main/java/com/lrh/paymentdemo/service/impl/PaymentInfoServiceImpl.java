package com.lrh.paymentdemo.service.impl;

;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.lrh.paymentdemo.entity.PaymentInfo;
import com.lrh.paymentdemo.enums.PayType;
import com.lrh.paymentdemo.mapper.PaymentInfoMapper;
import com.lrh.paymentdemo.service.PaymentInfoService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Override
    public void createPaymentInfo(Transaction transaction) {

        log.info("记录支付日志");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(transaction.getOutTradeNo());
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transaction.getTransactionId());
        paymentInfo.setTradeType(transaction.getTradeType().name());
        paymentInfo.setTradeState(transaction.getTradeState().name());
        paymentInfo.setPayerTotal(transaction.getAmount().getPayerTotal());
        paymentInfo.setContent(String.valueOf(transaction));

        baseMapper.insert(paymentInfo);

    }

    @Override
    public void createPaymentInfo(Map<String, String> params) {
        log.info("记录支付日志");

        String orderNo = params.get("out_trade_no");
        String transactionId = params.get("trade_no");
        String tradeState = params.get("trade_status");
        String totalAmount = params.get("total_amount");
        int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();
        String body = params.get("body");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.ALIPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType("电脑网站支付");
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(totalAmountInt);

        Gson gson = new Gson();
        String json = gson.toJson(params, HashMap.class);
        paymentInfo.setContent(json);

        baseMapper.insert(paymentInfo);
    }

    @Override
    public void createPaymentInfo(AlipayTradeQueryResponseModel alipayTradeQueryResponseModel) {
        log.info("记录支付日志");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(alipayTradeQueryResponseModel.getOutTradeNo());
        paymentInfo.setPaymentType(PayType.ALIPAY.getType());
        paymentInfo.setTransactionId(alipayTradeQueryResponseModel.getTradeNo());
        paymentInfo.setTradeType("电脑网站支付");
        paymentInfo.setTradeState(alipayTradeQueryResponseModel.getTradeStatus());
        int total = new BigDecimal(Objects.requireNonNull(alipayTradeQueryResponseModel.getTotalAmount())).multiply(new BigDecimal("100")).intValue();
        paymentInfo.setPayerTotal(total);
        paymentInfo.setContent(String.valueOf(alipayTradeQueryResponseModel));

        baseMapper.insert(paymentInfo);
    }
}
