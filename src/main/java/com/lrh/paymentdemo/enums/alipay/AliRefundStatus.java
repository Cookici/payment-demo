package com.lrh.paymentdemo.enums.alipay;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AliRefundStatus {

    /**
     * 退款关闭
     */
    CLOSED("TRADE_CLOSED"),

    /**
     * 退款处理中
     */
    REFUND_SUCCESS("REFUND_SUCCESS"),

    /**
     * 退款异常
     */
    REFUND_ERROR("REFUND_ERROR");

    /**
     * 类型
     */
    private final String type;
}
