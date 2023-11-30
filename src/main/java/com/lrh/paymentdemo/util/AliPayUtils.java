package com.lrh.paymentdemo.util;

import com.alipay.v3.model.AlipayTradeRefundApplyModel;
import com.alipay.v3.model.AlipayTradeRefundModel;
import com.lrh.paymentdemo.entity.RefundInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.util
 * @ClassName: AliPayUtils
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/30 17:05
 */
@Component
public class AliPayUtils {

    /**
     * 申请退款信息
     * @param orderNo
     * @param reason
     * @param refundInfo
     * @return
     */
    public AlipayTradeRefundModel getAlipayTradeRefundModel(String orderNo, String reason, RefundInfo refundInfo) {
        AlipayTradeRefundModel alipayTradeRefundModel = new AlipayTradeRefundModel();
        alipayTradeRefundModel.setOutTradeNo(orderNo);
        BigDecimal total = new BigDecimal(refundInfo.getRefund().toString()).divide(new BigDecimal("100"));
        alipayTradeRefundModel.setRefundAmount(String.valueOf(total));
        alipayTradeRefundModel.setRefundReason(reason);
        alipayTradeRefundModel.setRefundCurrency("CNY");
        return alipayTradeRefundModel;
    }

}
