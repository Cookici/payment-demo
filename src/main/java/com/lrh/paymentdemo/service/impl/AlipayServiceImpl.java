package com.lrh.paymentdemo.service.impl;


import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.api.AlipayDataDataserviceBillDownloadurlApi;
import com.alipay.v3.api.AlipayTradeApi;
import com.alipay.v3.api.AlipayTradeFastpayRefundApi;
import com.alipay.v3.model.*;
import com.alipay.v3.util.GenericExecuteApi;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.entity.RefundInfo;
import com.lrh.paymentdemo.enums.OrderStatus;
import com.lrh.paymentdemo.enums.alipay.AliRefundStatus;
import com.lrh.paymentdemo.enums.alipay.AliTradeState;
import com.lrh.paymentdemo.service.AlipayService;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.lrh.paymentdemo.service.PaymentInfoService;
import com.lrh.paymentdemo.service.RefundInfoService;
import com.lrh.paymentdemo.util.AliPayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.service.impl
 * @ClassName: AlipayServiceImpl
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/28 18:12
 */
@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private Environment environment;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private ApiClient apiClient;

    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private AliPayUtils aliPayUtils;

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String tradeCreate(Long productId) {

        //生成订单
        log.info("生成订单");
        OrderInfo orderInfo = orderInfoService.createOrderByProductIdAli(productId);
        BigDecimal total =
                new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100"));
        //实例化客户端
        GenericExecuteApi api = new GenericExecuteApi();
        //设置业务参数
        String method = "alipay.trade.page.pay";
        String httpMethod = "POST";
        Map<String, Object> bizParams = new HashMap<>();
        Map<String, Object> otherParams = new HashMap<>();
        otherParams.put("out_trade_no", orderInfo.getOrderNo());
        otherParams.put("product_code", "FAST_INSTANT_TRADE_PAY");
        otherParams.put("total_amount", total);
        otherParams.put("subject", orderInfo.getTitle());
        bizParams.put("biz_content", otherParams);
        bizParams.put("return_url", environment.getProperty("alipay.return-url"));
        bizParams.put("notify_url", environment.getProperty("alipay.notify-url"));

        String response = null;
        try {
            response = api.pageExecute(method, httpMethod, bizParams);
        } catch (ApiException e) {
            log.error("生成订单失败 ====> {}", e.getResponseBody());
            throw new RuntimeException(e);
        }

        log.info("response ====> {}", response);

        return response;
    }

    /**
     * 处理订单
     *
     * @param params
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(Map<String, String> params) {

        log.info("开始处理Alipay订单");
        String orderNo = params.get("out_trade_no");


        if (lock.tryLock()) {
            try {
                //处理重复的通知
                //接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的
                String orderStatus = orderInfoService.getOrderStatusAli(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);

                //记录支付日志
                paymentInfoService.createPaymentInfo(params);
            } finally {
                //主动释放锁
                lock.unlock();
            }
        }

    }

    /**
     * 用户取消订单
     *
     * @param orderNo
     */
    @Override
    public void cancelOrder(String orderNo) {

        //调用支付宝提供的统一收单交易关闭接口
        closeOrder(orderNo);

        //更新用户订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }

    /**
     * 查询订单
     *
     * @param orderNo
     * @return
     */
    @Override
    public AlipayTradeQueryResponseModel queryOrder(String orderNo) {

        log.info("查单接口调用 ====> {}", orderNo);

        AlipayTradeApi api = new AlipayTradeApi();
        api.setApiClient(apiClient);
        AlipayTradeQueryModel alipayTradeQueryModel = new AlipayTradeQueryModel();
        alipayTradeQueryModel.setOutTradeNo(orderNo);
        AlipayTradeQueryResponseModel query = null;
        try {
            query = api.query(alipayTradeQueryModel);
            if (Objects.equals(query.getOutTradeNo(), orderNo)) {
                log.info("查询订单成功 =====> {} ", query);
            } else {
                throw new RuntimeException("商品号异常,查询订单失败...");
            }
        } catch (Exception e) {
            return query;
        }

        return query;
    }

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单为创建，则更新商户端订单状态
     * 如果订单已支付，则更新商户端订单状态
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     *
     * @param orderNo
     */
    @Override
    public void checkOrderStatus(String orderNo) {
        log.warn("根据订单号核实订单状态 ===> {}", orderNo);

        //调用支付宝查单接口
        AlipayTradeQueryResponseModel alipayTradeQueryResponseModel = queryOrder(orderNo);
        if (alipayTradeQueryResponseModel == null) {
            log.warn("核实订单未创建 ===> {}", orderNo);
            //更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }

        if (alipayTradeQueryResponseModel != null && AliTradeState.SUCCESS.getType().equals(alipayTradeQueryResponseModel.getTradeStatus())) {
            log.warn("核实订单已支付 ====> {}", orderNo);
            //如果确认订单已支付则更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(alipayTradeQueryResponseModel);
        }

        if (alipayTradeQueryResponseModel != null && AliTradeState.NOTPAY.getType().equals(alipayTradeQueryResponseModel.getTradeStatus())) {
            log.warn("核实订单未支付 ====> {}", orderNo);
            //如果订单未支付,调用关单接口
            closeOrder(orderNo);
            //更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 支付宝退款
     *
     * @param orderNo
     * @param reason
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(String orderNo, String reason) {

        log.info("调用退款api");
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);


        AlipayTradeApi api = new AlipayTradeApi();
        api.setApiClient(apiClient);


        AlipayTradeRefundModel alipayTradeRefundApplyModel = aliPayUtils.getAlipayTradeRefundModel(orderNo, reason, refundInfo);
        AlipayTradeRefundResponseModel refund = null;
        try {
            refund = api.refund(alipayTradeRefundApplyModel);
            if (Objects.equals(refund.getOutTradeNo(), orderNo)) {
                log.info("申请退款成功 =====> {} ", refund);

                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);

                //更新退款单
                refundInfoService.updateRefund(refund, refundInfo.getRefundNo(), AliRefundStatus.REFUND_SUCCESS.getType());

            } else {
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_ABNORMAL);
                refundInfoService.updateRefund(refund, refundInfo.getRefundNo(), AliRefundStatus.REFUND_ERROR.getType());
                throw new RuntimeException("商品号异常,退款失败...");
            }
        } catch (ApiException e) {
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_ABNORMAL);
            refundInfoService.updateRefund(refund, refundInfo.getRefundNo(), AliRefundStatus.REFUND_ERROR.getType());
            throw new RuntimeException(e);
        }

    }

    @Override
    public AlipayTradeFastpayRefundQueryResponseModel queryRefound(String orderNo) {

        AlipayTradeFastpayRefundApi api = new AlipayTradeFastpayRefundApi();
        api.setApiClient(apiClient);
        AlipayTradeFastpayRefundQueryModel alipayTradeFastpayRefundQueryModel = new AlipayTradeFastpayRefundQueryModel();
        alipayTradeFastpayRefundQueryModel.setOutTradeNo(orderNo);
        alipayTradeFastpayRefundQueryModel.setOutRequestNo(orderNo);
        AlipayTradeFastpayRefundQueryResponseModel query = null;
        try {
            query = api.query(alipayTradeFastpayRefundQueryModel);
            if (Objects.equals(query.getOutTradeNo(), orderNo)) {
                log.info("查询退款成功 =====> {} ", query);
            } else {
                throw new RuntimeException("商品号异常,查询退款失败...");
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return query;
    }

    @Override
    public String queryBill(String billDate, String type) {

        AlipayDataDataserviceBillDownloadurlApi api = new AlipayDataDataserviceBillDownloadurlApi();
        api.setApiClient(apiClient);

        AlipayDataDataserviceBillDownloadurlQueryResponseModel query = null;
        try {
            query = api.query(type, billDate, null);
            log.info("获取url成功 ===> {}", query);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return query.getBillDownloadUrl();
    }


    /**
     * 关单接口调用
     *
     * @param orderNo
     */
    private void closeOrder(String orderNo) {

        //扫码和输入账号密码才创建订单 才可以关闭

        log.info("关单接口调用,订单号 =====> {}", orderNo);

        AlipayTradeApi api = new AlipayTradeApi();
        api.setApiClient(apiClient);
        AlipayTradeCloseModel alipayTradeCloseModel = new AlipayTradeCloseModel();
        alipayTradeCloseModel.setOutTradeNo(orderNo);
        AlipayTradeCloseResponseModel close = null;
        try {
            close = api.close(alipayTradeCloseModel);
            if (Objects.equals(close.getOutTradeNo(), orderNo)) {
                log.info("关单成功 =====> {} ", close);
            } else {
                throw new RuntimeException("商品号异常,关单失败...");
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }
}
