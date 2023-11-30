package com.lrh.paymentdemo.service;

import com.alipay.v3.model.AlipayTradeRefundApplyResponseModel;
import com.alipay.v3.model.AlipayTradeRefundResponseModel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lrh.paymentdemo.entity.RefundInfo;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;

public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    void updateRefund(Refund refund);

    void updateRefund(RefundNotification refundNotification);

    void updateRefund(AlipayTradeRefundResponseModel refund, String refundNo, String type);
}
