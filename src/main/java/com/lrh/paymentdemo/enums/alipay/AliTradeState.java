package com.lrh.paymentdemo.enums.alipay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AliTradeState {

    /**
     * 支付成功
     */
    SUCCESS("TRADE_SUCCESS"),

    /**
     * 未支付
     */
    NOTPAY("WAIT_BUYER_PAY"),

    /**
     * 已关闭
     */
    CLOSED("TRADE_CLOSED"),

    /**
     * 转入退款
     */
    REFUND("REFUND");

    /**
     * 类型
     */
    private final String type;
}
