package com.lrh.paymentdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrh.paymentdemo.entity.Product;
import com.lrh.paymentdemo.mapper.ProductMapper;
import com.lrh.paymentdemo.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
