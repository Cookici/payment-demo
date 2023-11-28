package com.lrh.paymentdemo.controller;

import com.lrh.paymentdemo.config.WechatPayYmlConfig;
import com.lrh.paymentdemo.result.R;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.controller
 * @ClassName: TestController
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 16:54
 */
@Api(tags = "测试wx")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private WechatPayYmlConfig wechatPayYmlConfig;

    @GetMapping
    public R<String> getWxPayConfig() {
        return R.ok(wechatPayYmlConfig.getMchId());
    }

}
