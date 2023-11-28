package com.lrh.paymentdemo.util;

import com.lrh.paymentdemo.config.WechatPayYmlConfig;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.entity.RefundInfo;
import com.lrh.paymentdemo.enums.wxpay.WxNotifyType;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.http.*;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.util
 * @ClassName: WeChatPayUtils
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 22:15
 */
@Component
public class WeChatPayUtils {

    @Autowired
    private WechatPayYmlConfig wechatPayYmlConfig;

    /**
     * 获取验证码code_url
     * @param orderInfo
     * @return
     */
    public PrepayRequest getPrepayRequest(OrderInfo orderInfo) {
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(orderInfo.getTotalFee());
        amount.setCurrency("CNY");
        request.setAmount(amount);
        request.setAppid(wechatPayYmlConfig.getAppid());
        request.setMchid(wechatPayYmlConfig.getMchId());
        request.setDescription(orderInfo.getTitle());
        request.setNotifyUrl(wechatPayYmlConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        request.setOutTradeNo(orderInfo.getOrderNo());
        return request;
    }


    /**
     * 获取退款信息
     * @param refundInfo
     * @return
     */
    public CreateRequest getCreateRequest(RefundInfo refundInfo) {
        CreateRequest createRequest = new CreateRequest();
        AmountReq amount = new AmountReq();
        amount.setRefund(Long.valueOf(refundInfo.getRefund()));
        amount.setTotal(Long.valueOf(refundInfo.getTotalFee()));
        amount.setCurrency("CNY");
        createRequest.setAmount(amount);
        createRequest.setOutTradeNo(refundInfo.getOrderNo());
        createRequest.setOutRefundNo(refundInfo.getRefundNo());
        createRequest.setReason(refundInfo.getReason());
        createRequest.setNotifyUrl(wechatPayYmlConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));
        return createRequest;
    }



}
