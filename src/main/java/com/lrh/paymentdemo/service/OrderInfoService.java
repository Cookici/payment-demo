package com.lrh.paymentdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.enums.OrderStatus;

import java.util.List;

public interface OrderInfoService extends IService<OrderInfo> {

    OrderInfo createOrderByProductIdWx(Long productId);

    OrderInfo createOrderByProductIdAli(Long productId);

    void savaCodeUrl(String orderNo,String codeUrl);

    List<OrderInfo> listOrderByCreateTimeDesc();

    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    String getOrderStatusWx(String orderNo);

    List<OrderInfo> getNoPayOrderByDurationWx(int minutes);

    List<OrderInfo> getNoPayOrderByDurationAli(int minutes);

    OrderInfo getOrderByOrderNo(String orderNo);

    String getOrderStatusAli(String orderNo);
}
