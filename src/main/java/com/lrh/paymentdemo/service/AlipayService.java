package com.lrh.paymentdemo.service;

import com.alipay.v3.model.AlipayTradeFastpayRefundQueryResponseModel;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;

import java.util.Map;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.service
 * @ClassName: AlipayService
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/28 18:12
 */

public interface AlipayService {

    String tradeCreate(Long productId);

    void processOrder(Map<String, String> params);

    void cancelOrder(String orderNo);

    AlipayTradeQueryResponseModel queryOrder(String orderNo);

    void checkOrderStatus(String orderNo);

    void refund(String orderNo, String reason);

    AlipayTradeFastpayRefundQueryResponseModel queryRefound(String orderNo);

    String queryBill(String billDate, String type);
}
