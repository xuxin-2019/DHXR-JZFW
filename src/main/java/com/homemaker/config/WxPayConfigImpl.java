package com.homemaker.config;

import com.github.wxpay.sdk.WXPayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 微信支付配置实现类
 * 用于提供微信支付SDK所需的配置参数
 */
@Component
public class WxPayConfigImpl implements WXPayConfig {

    private static final Logger logger = LoggerFactory.getLogger(WxPayConfigImpl.class);
    
    @Value("${wechat.pay.appid}")
    private String appid;
    
    @Value("${wechat.pay.mchid}")
    private String mchid;
    
    @Value("${wechat.pay.key}")
    private String key;
    
    @Value("${wechat.pay.cert_path}")
    private String certPath;
    
    @Override
    public String getAppID() {
        return appid;
    }
    
    @Override
    public String getMchID() {
        return mchid;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public InputStream getCertStream() {
        try {
            // 在实际应用中，应该读取真实的证书文件
            File file = new File(certPath);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            // 测试环境可以返回空的输入流
            return new ByteArrayInputStream(new byte[0]);
        } catch (Exception e) {
            logger.error("获取证书失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public int getHttpConnectTimeoutMs() {
        return 8000;
    }
    
    @Override
    public int getHttpReadTimeoutMs() {
        return 10000;
    }
}