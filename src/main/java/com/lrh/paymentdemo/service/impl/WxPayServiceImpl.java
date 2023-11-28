package com.lrh.paymentdemo.service.impl;


import com.google.gson.Gson;
import com.lrh.paymentdemo.config.WechatPayYmlConfig;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.entity.RefundInfo;
import com.lrh.paymentdemo.enums.OrderStatus;
import com.lrh.paymentdemo.enums.wxpay.WxApiType;
import com.lrh.paymentdemo.enums.wxpay.WxTradeState;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.lrh.paymentdemo.service.PaymentInfoService;
import com.lrh.paymentdemo.service.RefundInfoService;
import com.lrh.paymentdemo.service.WxPayService;


import com.lrh.paymentdemo.util.WeChatPayUtils;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.http.*;
import com.wechat.pay.java.core.http.okhttp.OkHttpClientAdapter;
import com.wechat.pay.java.core.util.GsonUtil;
import com.wechat.pay.java.core.util.IOUtil;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.service.impl
 * @ClassName: WxPayServiceImpl
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 19:01
 */
@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private WeChatPayUtils weChatPayUtils;

    @Autowired
    private WechatPayYmlConfig wechatPayYmlConfig;

    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private Config config;

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public Map<String, Object> nativePay(Long productId) {

        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        String codeUrl = orderInfo.getCodeUrl();
        if (orderInfo != null && StringUtils.hasText(codeUrl)) {
            log.info("二维码已保存 codeUrl ===> {} ", codeUrl);
            Map<String, Object> map = new HashMap<>(2);
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }
        //构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        PrepayRequest request = weChatPayUtils.getPrepayRequest(orderInfo);
        // 调用下单方法，得到应答
        PrepayResponse response = null;
        try {
            response = service.prepay(request);
            codeUrl = response.getCodeUrl();
            // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
            log.info("code_url ====> {}", codeUrl);
        } catch (Exception e) {
            log.info("nativePay Error ====> {}", e.getMessage());
            throw new RuntimeException(e);
        }

        //保存二维码
        String orderNo = orderInfo.getOrderNo();
        orderInfoService.savaCodeUrl(orderNo, codeUrl);

        Map<String, Object> map = new HashMap<>(2);
        map.put("codeUrl", codeUrl);
        map.put("orderNo", orderNo);
        return map;

    }

    @Override
    public void processOrder(Transaction transaction) {

        log.info("处理订单");
        String orderNo = transaction.getOutTradeNo();

        /*
         在对业务数据进行状态检查和处理之前，
         要采用数据锁进行并发控制，
         以避免函数重入造成的数据混乱。
        */
        //尝试获取锁：成功获取则立即返回true，获取失败则立即返回false。不会一直等待锁的释放
        if (lock.tryLock()) {
            try {
                //处理重复的通知
                //接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                //记录支付日志
                paymentInfoService.createPaymentInfo(transaction);
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

        //调用微信支付的关闭订单接口
        closeOrder(orderNo);

        //更新商户短的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);

    }

    /**
     * 查单
     *
     * @param orderNo
     * @return
     */
    @Override
    public Transaction queryOrder(String orderNo) {
        log.info("查单接口调用 ====> {}", orderNo);
        NativePayService service = new NativePayService.Builder().config(config).build();
        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();
        queryOrderByOutTradeNoRequest.setMchid(wechatPayYmlConfig.getMchId());
        queryOrderByOutTradeNoRequest.setOutTradeNo(orderNo);
        return service.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);
    }

    /**
     * 根据订单号查询微信支付查单接口，核实订单状态
     * 如果订单已支付，则更新商户端订单状态
     * 如果订单未支付，则调用关单接口关闭订单，并更新商户端订单状态
     *
     * @param orderNo
     */
    @Override
    public void checkOrderStatus(String orderNo) {

        log.warn("根据订单号核实订单状态 ===> {}", orderNo);

        //调用微信查单接口
        Transaction transaction = queryOrder(orderNo);

        Transaction.TradeStateEnum tradeState = transaction.getTradeState();
        if (WxTradeState.SUCCESS.getType().equals(tradeState.name())) {
            log.warn("核实订单已支付 ====> {}", orderNo);
            //如果确认订单已支付则更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(transaction);
        }

        if (WxTradeState.NOTPAY.getType().equals(tradeState.name())) {
            log.warn("核实订单未支付 ====> {}", orderNo);
            //如果订单未支付,调用关单接口
            closeOrder(orderNo);
            //更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }


    }

    @Override
    public void refund(String orderNo, String reason) {

        log.info("创建退款单记录");
        //根据订单编号创建退款单
        RefundInfo refundInfo = refundInfoService.createRefundByOrderNo(orderNo, reason);

        log.info("调用退款API");
        RefundService service = new RefundService.Builder().config(config).build();
        CreateRequest createRequest = weChatPayUtils.getCreateRequest(refundInfo);

        Refund refund = null;
        try {
            refund = service.create(createRequest);
            log.info("申请退款成功 ===> {}", refund);

            //更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);

            //更新退款单
            refundInfoService.updateRefund(refund);

        } catch (Exception e) {
            throw new RuntimeException("退款异常 ===> " + e);
        }


    }

    @Override
    public Refund queryRefund(String refundNo) {
        log.info("查退款接口调用 ====> {}", refundNo);
        RefundService service = new RefundService.Builder().config(config).build();
        QueryByOutRefundNoRequest queryRefundByOutRefundNoRequest = new QueryByOutRefundNoRequest();
        queryRefundByOutRefundNoRequest.setOutRefundNo(refundNo);
        return service.queryByOutRefundNo(queryRefundByOutRefundNoRequest);
    }

    @Override
    public void processRefund(RefundNotification refundNotification) {
        log.info("退款订单");

        String orderNo = refundNotification.getOutTradeNo();

        if (lock.tryLock()) {

            try {
                String orderStatus = orderInfoService.getOrderStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                    return;
                }

                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);

                //更新退款单
                refundInfoService.updateRefund(refundNotification);

            } finally {
                lock.unlock();
            }


        }


    }

    @Override
    public String queryBill(String billDate, String type) {

        log.warn("申请账单接口调用 ====> {}", billDate);

        String url = "";
        if ("tradebill".equals(type)) {
            url = WxApiType.TRADE_BILLS.getType();
        } else if ("fundflowbill".equals(type)) {
            url = WxApiType.FUND_FLOW_BILLS.getType();
        } else {
            throw new RuntimeException("不支持的账单类型");
        }


        url = wechatPayYmlConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);

        HttpClient httpClient = new DefaultHttpClientBuilder().config(config).build();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addHeader(Constant.ACCEPT, MediaType.APPLICATION_JSON.getValue());
        HttpResponse<Object> response = httpClient.get(httpHeaders, url, Object.class);
        ResponseBody body = response.getBody();
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(body.toString(), HashMap.class);
        log.info("map ===> {}", map);
        String bodyString = (String) map.get("body");
        Map<String, Object> bodyMap = gson.fromJson(bodyString, HashMap.class);
        String downloadUrl = (String) bodyMap.get("download_url");
        log.info("downloadUrl ===> {}", downloadUrl);
        return downloadUrl;
    }


    @Override
    public String downloadBill(String billDate, String type) {

        log.info("下载账单接口调用 {} {}", billDate, type);
        String downloadUrl = queryBill(billDate, type);
        HttpClient httpClient = new DefaultHttpClientBuilder().config(config).build();
        InputStream inputStream = null;
        String respBody = null;
        try {
            inputStream = httpClient.download(downloadUrl);
            respBody = IOUtil.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }

        return respBody;
    }


    /**
     * 微信关单接口调用
     *
     * @param orderNo
     */
    private void closeOrder(String orderNo) {

        log.info("关单接口被调用,订单号 =====> {}", orderNo);
        NativePayService service = new NativePayService.Builder().config(config).build();
        CloseOrderRequest closeOrderRequest = new CloseOrderRequest();
        closeOrderRequest.setMchid(wechatPayYmlConfig.getMchId());
        closeOrderRequest.setOutTradeNo(orderNo);
        log.info("closeOrderRequest ===> {}", closeOrderRequest);
        service.closeOrder(closeOrderRequest);

    }


}
