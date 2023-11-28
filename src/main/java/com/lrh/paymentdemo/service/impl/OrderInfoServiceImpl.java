package com.lrh.paymentdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrh.paymentdemo.entity.OrderInfo;
import com.lrh.paymentdemo.entity.Product;
import com.lrh.paymentdemo.enums.OrderStatus;
import com.lrh.paymentdemo.enums.PayType;
import com.lrh.paymentdemo.mapper.OrderInfoMapper;
import com.lrh.paymentdemo.mapper.ProductMapper;
import com.lrh.paymentdemo.service.OrderInfoService;
import com.lrh.paymentdemo.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public OrderInfo createOrderByProductId(Long productId) {

        //查找已存在但未支付的订单
        OrderInfo orderInfo = getNoPayOrderByProduct(productId);
        if (orderInfo != null) {
            return orderInfo;
        }


        //获取商品信息
        Product product = productMapper.selectById(productId);

        //生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        log.info("生成订单：{}", orderInfo);

        //存入数据库
        baseMapper.insert(orderInfo);

        return orderInfo;
    }

    /**
     * 存储订单二维码
     *
     * @param orderNo
     * @param codeUrl
     */
    @Override
    public void savaCodeUrl(String orderNo, String codeUrl) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.update(orderInfo, new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
    }

    /**
     * 时间倒序订单列表
     *
     * @return
     */
    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        return baseMapper.selectList(new LambdaQueryWrapper<OrderInfo>().orderByDesc(OrderInfo::getCreateTime));
    }

    /**
     * 更新订单状态
     *
     * @param orderNo
     * @param orderStatus
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态 ===> {}", orderStatus.getType());
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        baseMapper.update(orderInfo, new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
    }

    @Override
    public String getOrderStatus(String orderNo) {

        OrderInfo orderInfo = baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (orderInfo == null) {
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    /**
     * 查询创建超过minutes分钟并且为支付的订单
     *
     * @param minutes
     * @return
     */
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {

        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getOrderStatus, OrderStatus.NOTPAY.getType());
        queryWrapper.le(OrderInfo::getCreateTime, instant);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        return baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
    }

    /**
     * 防止生成重复订单对象
     *
     * @param productId
     * @return
     */
    private OrderInfo getNoPayOrderByProduct(Long productId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getProductId, productId);
        queryWrapper.eq(OrderInfo::getOrderStatus, OrderStatus.NOTPAY.getType());
        // queryWrapper.eq(OrderInfo::getUserId,userId);
        return baseMapper.selectOne(queryWrapper);
    }


}
