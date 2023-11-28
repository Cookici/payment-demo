package com.lrh.paymentdemo.controller;

import com.lrh.paymentdemo.result.R;
import com.lrh.paymentdemo.service.WxPayService;
import com.lrh.paymentdemo.util.HttpUtils;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.controller
 * @ClassName: WxPayController
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 19:00
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付Api")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private Config config;

    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public R<Map<String, Object>> nativePay(@PathVariable Long productId) {
        Map<String, Object> result = wxPayService.nativePay(productId);
        log.info("发起支付请求");
        return R.ok(result);
    }


    @PostMapping("native/notify")
    public ResponseEntity<Map<String, Object>> nativeNotify(HttpServletRequest request, HttpServletResponse response) {

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signType(request.getHeader("Wechatpay-Signature-Type"))
                .body(HttpUtils.readData(request))
                .build();

        log.info("requestParam ====> {}", requestParam);

        //签名验证
        NotificationParser parser = new NotificationParser((NotificationConfig) config);
        Transaction transaction = null;
        Map<String, Object> responseBody = new HashMap<>();
        try {
            //验签、解密并转换成 Transaction
            transaction = parser.parse(requestParam, Transaction.class);
            log.info("验签成功 OvO~~ transaction ====> {}", transaction);

            //处理订单
            wxPayService.processOrder(transaction);

        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed", e);
            responseBody.put("code", "ERROR");
            responseBody.put("message", e);
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            // 出现运行时异常
            log.error("runtimeException failed", e);
            responseBody.put("code", "ERROR");
            responseBody.put("message", e);
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<>(HttpStatus.OK);
    }


    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public R<Object> cancel(@PathVariable String orderNo) {

        log.info("取消订单");

        wxPayService.cancelOrder(orderNo);
        return R.ok().message("订单已取消");
    }

    @ApiOperation("查询订单")
    @GetMapping("/query/{orderNo}")
    public R<Map<String, Object>> queryOrder(@PathVariable String orderNo) {

        log.info("查询订单");
        Transaction transaction = wxPayService.queryOrder(orderNo);
        Map<String, Object> map = new HashMap<>(1);
        map.put("result", transaction);
        return R.ok(map).message("查询成功");
    }

    @ApiOperation("申请退款")
    @PostMapping("/refunds/{orderNo}/{reason}")
    public R<Object> refunds(@PathVariable String orderNo, @PathVariable String reason) {

        log.info("申请退款");
        wxPayService.refund(orderNo, reason);

        return R.ok();
    }


    @ApiOperation("查询退款")
    @GetMapping("/query-refund/{refundNo}")
    public R<Map<String, Object>> queryRefound(@PathVariable String refundNo) {

        log.info("查询退款");

        Refund refund = wxPayService.queryRefund(refundNo);
        Map<String, Object> resultRefund = new HashMap<>(1);
        resultRefund.put("result", refund);
        return R.ok(resultRefund);
    }


    @PostMapping("refunds/notify")
    public ResponseEntity<Map<String, Object>> refundNotify(HttpServletRequest request, HttpServletResponse response) {

        log.info("退款通知执行");
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signType(request.getHeader("Wechatpay-Signature-Type"))
                .body(HttpUtils.readData(request))
                .build();

        //签名验证
        NotificationParser parser = new NotificationParser((NotificationConfig) config);
        RefundNotification refundNotification = null;
        Map<String, Object> responseBody = new HashMap<>();
        try {
            //验签、解密并转换成 Transaction
            refundNotification = parser.parse(requestParam, RefundNotification.class);
            log.info("验签成功 OvO~~ refundNotification ====> {}", refundNotification);

            //处理退款单
            wxPayService.processRefund(refundNotification);

        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed", e);
            responseBody.put("code", "ERROR");
            responseBody.put("message", e);
            return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            // 出现运行时异常
            log.error("runtimeException failed", e);
            responseBody.put("code", "ERROR");
            responseBody.put("message", e);
            return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<>(HttpStatus.OK);

    }

    @ApiOperation("获取账单url")
    @GetMapping("/querybill/{billDate}/{type}")
    public R<Map<String,Object>> queryTradeBill(
            @PathVariable String billDate,
            @PathVariable String type) {
        log.info("获取账单url");
        String downloadUrl = wxPayService.queryBill(billDate, type);
        Map<String, Object> result = new HashMap<>();
        result.put("downloadUrl", downloadUrl);
        return R.ok(result).message("获取账单url成功");
    }


    @ApiOperation("下载账单")
    @GetMapping("/downloadbill/{billDate}/{type}")
    public R<Map<String,Object>> downloadBill(
            @PathVariable String billDate,
            @PathVariable String type) {
        log.info("下载账单url");
        String downloadUrl = wxPayService.downloadBill(billDate, type);
        Map<String, Object> result = new HashMap<>();
        result.put("result", downloadUrl);
        return R.ok(result).message("请求下载账单成功");
    }


}
