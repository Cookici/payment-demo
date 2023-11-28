package com.lrh.paymentdemo.service;

import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;

import java.util.Map;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.service
 * @ClassName: WxPayService
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 19:01
 */

public interface WxPayService {

    Map<String, Object> nativePay(Long productId);


    void processOrder(Transaction transaction);

    void cancelOrder(String orderNo);

    Transaction queryOrder(String orderNo);

    void checkOrderStatus(String orderNo);

    void refund(String orderNo, String reason);

    Refund queryRefund(String refundNo);

    void processRefund(RefundNotification refundNotification);

    String queryBill(String billDate, String type);

    String downloadBill(String billDate, String type);
}
