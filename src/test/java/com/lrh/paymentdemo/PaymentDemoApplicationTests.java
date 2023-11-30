package com.lrh.paymentdemo;

import com.lrh.paymentdemo.config.AlipayYmlConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class PaymentDemoApplicationTests {

@Autowired
private Environment alipayYmlConfig;

    @Test
    void test() {
        System.out.println(alipayYmlConfig.getProperty("alipay.app-id"));
    }

}
