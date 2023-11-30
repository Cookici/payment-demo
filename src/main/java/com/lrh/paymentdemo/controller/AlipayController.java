package com.lrh.paymentdemo.controller;

import com.alipay.v3.ApiException;
import com.alipay.v3.model.AlipayTradeFastpayRefundQueryResponseModel;
import com.alipay.v3.model.AlipayTradeQueryResponseModel;
import com.alipay.v3.util.AlipaySignature;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.result.R;
import com.lrh.paymentdemo.service.AlipayService;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.controller
 * @ClassName: AlipayController
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/28 18:09
 */
@CrossOrigin
@RestController
@RequestMapping("/api/ali-pay")
@Api(tags = "网站支付宝支付")
@Slf4j
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private Environment environment;

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation("统一收单下单并支付页面接口")
    @PostMapping("/trade/page/pay/{productId}")
    public R<Map<String, Object>> tradePagePay(@PathVariable Long productId) {

        log.info("统一收单下单并支付页面接口调用");

        //1.支付宝开放平台接收request请求对象
        //2.生成一个html形式form表单
        //3.包含自动提交的脚本
        String formString = alipayService.tradeCreate(productId);

        //将form表单字符串返回给前端程序，之后前端会调用自动提交脚本，进行表单的提交
        //表单自动提交到action属性指向的支付宝开放平台，从而为用户展示一个支付页面
        Map<String, Object> result = new HashMap<>();
        result.put("formStr", formString);
        return R.ok(result);
    }

    @ApiOperation("支付通知")
    @PostMapping("/trade/notify")
    public String tradeNotify(@RequestParam Map<String, String> params) {
        log.info("支付通知到达...");
        log.info("通知参数 ===> {}", params);
        String result = "failure";

        //异步通知验签
        try {
            boolean signVerified = AlipaySignature.verifyV1(params, environment.getProperty("alipay.alipay-public-key"), "utf-8", "RSA2");
            if (!signVerified) {
                log.error("验签失败,签名有问题...");
                return result;
            }

            log.info("验签成功OvO~~~");

            //对支付结果中的业务内容进行二次校验
            //1.商家需要验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号
            String outTradeNo = params.get("out_trade_no");
            ;
            OrderInfo order = orderInfoService.getOrderByOrderNo(outTradeNo);
            if (order == null) {
                log.error("订单不存在!!!");
                return result;
            }

            //2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）
            String totalAmount = params.get("total_amount");
            int totalAmountInt = new BigDecimal(totalAmount).multiply(new BigDecimal("100")).intValue();
            int totalFeeInt = order.getTotalFee();
            if (totalAmountInt != totalFeeInt) {
                log.error("订单金额校验失败!!!");
                return result;
            }

            // 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）
            String sellerId = params.get("seller_id");
            String sellerIdProperty = environment.getProperty("alipay.seller-id");
            if (!Objects.equals(sellerId, sellerIdProperty)) {
                log.error("商家的PID校验失败!!!");
                return result;
            }

            //4.验证 app_id 是否为该商家本身
            String appId = params.get("app_id");
            String appIdProperty = environment.getProperty("alipay.app-id");
            if (!Objects.equals(appId, appIdProperty)) {
                log.error("appId校验失败!!!");
                return result;
            }

            /*
             * 交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功
             * 状态 TRADE_SUCCESS 的通知触发条件是商家开通的产品支持退款功能的前提下，买家付款成功。
             * 交易状态 TRADE_FINISHED 的通知触发条件是商家开通的产品不支持退款功能的前提下，买家付款成功；或者，商家开通的产品支持退款功能的前提下，交易已经成功并且已经超过可退款期限。
             */
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                log.error("支付未成功支付失败!!!");
                return result;
            }

            //处理订单
            alipayService.processOrder(params);

            //向支付宝返回成功结果
            result = "success";

        } catch (ApiException e) {
            log.error("验签过程出现异常 ====> {}", e.getResponseBody());
            throw new RuntimeException(e);
        }


        log.info("通知流程完成");
        return result;
    }


    /**
     * 用户取消订单
     *
     * @param orderNo
     * @return
     */
    @ApiOperation("用户取消订单")
    @PostMapping("/trade/close/{orderNo}")
    public R<Object> cancel(@PathVariable String orderNo) {

        log.info("用户取消订单");

        alipayService.cancelOrder(orderNo);


        return R.ok().message("订单已取消");
    }


    @ApiOperation("查询订单")
    @GetMapping("/trade/query/{orderNo}")
    public R<Map<String, Object>> queryOrder(@PathVariable String orderNo) {

        log.info("查询订单");
        AlipayTradeQueryResponseModel alipayTradeQueryResponseModel = alipayService.queryOrder(orderNo);
        Map<String, Object> map = new HashMap<>(1);
        map.put("result", alipayTradeQueryResponseModel);
        return R.ok(map).message("查询成功");
    }

    @ApiOperation("申请退款")
    @PostMapping("/trade/refund/{orderNo}/{reason}")
    public R refunds(@PathVariable String orderNo, @PathVariable String reason) {
        log.info("申请退款");
        alipayService.refund(orderNo, reason);
        return null;
    }


    @ApiOperation("查询退款，测试用")
    @GetMapping("/trade/fastpay/refund/{orderNo}")
    public R<Map<String, Object>> queryRefund(@PathVariable String orderNo) {

        AlipayTradeFastpayRefundQueryResponseModel queryResult = alipayService.queryRefound(orderNo);
        Map<String, Object> result = new HashMap<>();
        result.put("result", queryResult);
        return R.ok(result).message("查询成功");
    }

    /**
     * 根据账单类型和日期获取账单url地址
     * @param billDate
     * @param type
     * @return
     */
    @ApiOperation("获取账单url")
    @GetMapping("/bill/downloadurl/query/{billDate}/{type}")
    public R<Map<String,Object>> queryTradeBill(
            @PathVariable String billDate,
            @PathVariable String type) {
        log.info("获取账单url");
        String downloadUrl = alipayService.queryBill(billDate, type);
        Map<String, Object> result = new HashMap<>();
        result.put("downloadUrl", downloadUrl);
        return R.ok(result).message("获取账单url成功");
    }

}
