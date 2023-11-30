package com.lrh.paymentdemo.task;

import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.service.AlipayService;
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
 * @ClassName: Alii
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/30 15:55
 */
@Slf4j
@Component
public class AliPayTask {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private AlipayService alipayService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void taskForQueryOrder() {

        log.info("alipay taskForQueryOrder执行...");
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDurationAli(5);

        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ====> {}", orderNo);

            //核实订单状态，调用阿里支付查单接口
            alipayService.checkOrderStatus(orderNo);
        }
    }


}
