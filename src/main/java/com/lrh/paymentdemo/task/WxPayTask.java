package com.lrh.paymentdemo.task;

import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.lrh.paymentdemo.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.task
 * @ClassName: WxPayTask
 * @Author: 63283
 * @Description: 秒 分 时 日 月 周
 * *:每秒都执行
 * 1-3:从第1秒开始执行，到第3秒结束执行
 * 0/3:从第0秒开始，每隔3秒执行1次
 * 1,2,3:在指定的第1、2、3秒执行
 * ?:不指定
 * 日和周不能同时制定，指定其中之一，则另一个设置为?
 * @Date: 2023/11/27 21:17
 */
@Slf4j
@Component
public class WxPayTask {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private WxPayService wxPayService;


    @Scheduled(cron = "0/30 * * * * ?")
    public void taskForQueryOrder() {
        log.info("taskForQueryOrder执行...");
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(5);

        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ====> {}", orderNo);

            //核实订单状态，调用微信支付查单接口
            wxPayService.checkOrderStatus(orderNo);
        }
    }


}
