package com.lrh.paymentdemo.config;

import com.alipay.v3.ApiClient;
import com.alipay.v3.ApiException;
import com.alipay.v3.util.model.AlipayConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo
 * @ClassName: AlipayYmlConfig
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/28 17:38
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayYmlConfig {

    @Autowired
    private Environment environment;

    @Bean
    public ApiClient alipayClient() throws ApiException {

        ApiClient defaultClient = com.alipay.v3.Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://openapi.alipay.com");

        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(environment.getProperty("alipay.gateway-url"));
        //设置应用ID
        alipayConfig.setAppId(environment.getProperty("alipay.app-id"));
        //设置应用私钥
        alipayConfig.setPrivateKey(environment.getProperty("alipay.merchant-private-key"));
        //设置支付宝公匙
        alipayConfig.setAlipayPublicKey(environment.getProperty("alipay.alipay-public-key"));

        defaultClient.setAlipayConfig(alipayConfig);

        return defaultClient;
    }

}
