package com.lrh.paymentdemo.result;

import lombok.Getter;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.result
 * @ClassName: REnum
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 15:37
 */
@Getter
public enum REnum {

    /**
     * SUCCESS 请求成功
     * FAIL 请求失败
     */
    SUCCESS(200,"请求成功"),
    FAIL(400, "请求失败");


    private final Integer code;
    private final String message;

    REnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
