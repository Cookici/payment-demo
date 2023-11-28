package com.lrh.paymentdemo.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.config
 * @ClassName: MyBatisPlusConfig
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 16:51
 */
@Configuration
@MapperScan("com.lrh.paymentdemo.mapper")
@EnableTransactionManagement
public class MyBatisPlusConfig {

}
