package com.lrh.paymentdemo.controller;

import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.enums.OrderStatus;
import com.lrh.paymentdemo.result.R;
import com.lrh.paymentdemo.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.controller
 * @ClassName: OrderInfoController
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/27 12:23
 */
@CrossOrigin
@Api(tags = "商品订单管理")
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;


    @ApiOperation("订单列表API")
    @GetMapping("/list")
    public R<Map<String, Object>> getOrderList() {
        List<OrderInfo> list = orderInfoService.listOrderByCreateTimeDesc();
        Map<String, Object> result = new HashMap<>(1);
        result.put("list", list);
        return R.ok(result);
    }

    @ApiOperation("查询订单状态")
    @GetMapping("/query-order-status/{orderNo}")
    public R<Object> queryOrderStatus(@PathVariable String orderNo) {
        String orderStatus = orderInfoService.getOrderStatusWx(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return R.ok().code(0).message("支付成功");
        }
        return R.ok().code(101).message("支付中");
    }

}
