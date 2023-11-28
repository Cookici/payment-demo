package com.lrh.paymentdemo.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.*;

/**
 * @ProjectName: payment-demo
 * @Package: com.lrh.paymentdemo.config
 * @ClassName: WechatPayAutoConfiguration
 * @Author: 63283
 * @Description:
 * @Date: 2023/11/26 18:05
 */
@Slf4j
@Configuration
public class WechatPayAutoConfiguration {

    @Autowired
    private WechatPayYmlConfig ymlConfig;


    /**
     * 自动更新证书
     *
     * @return RSAAutoCertificateConfig
     */
    @Bean
    public Config config() throws IOException {
        X509Certificate certificate = getCertificate(Files.newInputStream(Paths.get(ymlConfig.getCertPath())));
        log.info("==========证书序列号：{}，商户信息：{}", ymlConfig.getMchSerialNo(), certificate.getSubjectDN());
        log.info("==========加载微信私钥配置:{}", ymlConfig.getPrivateKey());
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(ymlConfig.getMchId())
                .privateKey(ymlConfig.getPrivateKey())
                .merchantSerialNumber(ymlConfig.getMchSerialNo())
                .apiV3Key(ymlConfig.getApiV3Key())
                .build();
    }


    /**
     * 获取证书 将文件流转成证书文件
     *
     * @param inputStream 证书文件
     * @return {@link X509Certificate} 获取证书
     */
    public static X509Certificate getCertificate(InputStream inputStream) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("证书已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("证书尚未生效", e);
        } catch (CertificateException e) {
            throw new RuntimeException("无效的证书", e);
        }
    }

}
