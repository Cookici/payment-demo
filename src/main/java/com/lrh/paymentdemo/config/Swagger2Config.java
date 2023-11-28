package com.lrh.paymentdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.config
 * @ClassName: Swagger2Config
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 15:25
 */
@Configuration
public class Swagger2Config {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("微信支付案例接口文档")
                        .build());
    }


}
