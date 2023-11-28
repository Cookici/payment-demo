package com.lrh.paymentdemo.config;

import com.wechat.pay.java.core.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.security.PrivateKey;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "wxpay")
public class WechatPayYmlConfig {

    /**
     * 商户号
     */
    private String mchId;
    /**
     * 商户API证书序列号
     */
    private String mchSerialNo;

    /**
     * 商户证书文件
     */
    private String certPath;

    /**
     * 商户私钥文件
     */
    private String privateKeyPath;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * APPID
     */
    private String appid;

    /**
     * 微信服务器地址
     */
    private String domain;

    /**
     * 接收结果通知地址
     */
    private String notifyDomain;


    /**
     * 获取商户的私钥文件
     *
     * @return
     */
    public PrivateKey getPrivateKey() {
        try {
            return PemUtil.loadPrivateKeyFromPath(privateKeyPath);
        } catch (Exception e) {
            throw new RuntimeException("私钥文件不存在", e);
        }
    }


}
