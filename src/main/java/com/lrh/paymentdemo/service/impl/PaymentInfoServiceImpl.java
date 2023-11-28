package com.lrh.paymentdemo.service.impl;

;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrh.paymentdemo.entity.PaymentInfo;
import com.lrh.paymentdemo.enums.PayType;
import com.lrh.paymentdemo.mapper.PaymentInfoMapper;
import com.lrh.paymentdemo.service.PaymentInfoService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
