package com.lrh.paymentdemo.result;

import lombok.Data;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.result
 * @ClassName: R
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 15:29
 */
@Data
public class R<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;


    private R() {
    }

    public static <T> R<T> build(T body,REnum rEnum) {
        R<T> r = new R<>();
        if (body != null) {
            r.setData(body);
        }
        r.setCode(rEnum.getCode());
        r.setMessage(rEnum.getMessage());
        return r;
    }


    public static<T> R<T> ok() {
        return build(null,REnum.SUCCESS);
    }

    public static<T> R<T> fail() {
        return build(null,REnum.FAIL);
    }

    public static<T> R<T> ok(T data) {
        return build(data,REnum.SUCCESS);
    }

    public static<T> R<T> fail(T data) {
        return build(data,REnum.FAIL);
    }

    public R<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public R<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}
