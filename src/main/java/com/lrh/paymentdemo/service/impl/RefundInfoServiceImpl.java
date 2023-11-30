package com.lrh.paymentdemo.service.impl;

import com.alipay.v3.model.AlipayTradeRefundApplyResponseModel;
import com.alipay.v3.model.AlipayTradeRefundResponseModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.entity.RefundInfo;
import com.lrh.paymentdemo.enums.alipay.AliRefundStatus;
import com.lrh.paymentdemo.mapper.RefundInfoMapper;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.lrh.paymentdemo.service.RefundInfoService;
import com.lrh.paymentdemo.util.OrderNoUtils;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {

        //根据订单号获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);

        //根据订单号生成退款订单
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setOrderNo(orderNo);
        refundInfo.setRefundNo(OrderNoUtils.getRefundNo());
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        refundInfo.setRefund(orderInfo.getTotalFee());
        refundInfo.setReason(reason);

        //保存退款订单
        baseMapper.insert(refundInfo);

        return refundInfo;
    }

    /**
     * 申请退款
     *
     * @param refund
     */
    @Override
    public void updateRefund(Refund refund) {
        //根据退款单编号修改退款单
        LambdaQueryWrapper<RefundInfo> refundInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        refundInfoLambdaQueryWrapper.eq(RefundInfo::getRefundNo, refund.getOutRefundNo());

        //设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(refund.getRefundId());
        refundInfo.setRefundStatus(refund.getStatus().name());

        //将全部结果存入数据库中
        refundInfo.setContentReturn(String.valueOf(refund));

        baseMapper.update(refundInfo, refundInfoLambdaQueryWrapper);
    }

    /**
     * 退款完成回调
     *
     * @param refundNotification
     */
    @Override
    public void updateRefund(RefundNotification refundNotification) {
        //根据退款单编号修改退款单
        LambdaQueryWrapper<RefundInfo> refundInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        refundInfoLambdaQueryWrapper.eq(RefundInfo::getRefundNo, refundNotification.getOutRefundNo());

        //设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundId(refundNotification.getRefundId());
        refundInfo.setRefundStatus(refundNotification.getRefundStatus().name());

        //将全部结果存入数据库中
        refundInfo.setContentNotify(String.valueOf(refundNotification));

        baseMapper.update(refundInfo, refundInfoLambdaQueryWrapper);
    }

    /**
     * 申请退款
     *
     * @param refund
     */
    @Override
    public void updateRefund(AlipayTradeRefundResponseModel refund, String refundNo, String type) {
        //根据退款单编号修改退款单
        LambdaQueryWrapper<RefundInfo> refundInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        refundInfoLambdaQueryWrapper.eq(RefundInfo::getRefundNo, refundNo);

        //设置要修改的字段
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setRefundStatus(type);

        //将全部结果存入数据库中
        refundInfo.setContentReturn(String.valueOf(refund));

        baseMapper.update(refundInfo, refundInfoLambdaQueryWrapper);
    }
}
