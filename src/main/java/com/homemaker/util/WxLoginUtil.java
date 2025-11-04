package com.homemaker.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 微信登录工具类
 */
@Component
public class WxLoginUtil {

    // 微信小程序登录接口
    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    
    // 生成自定义登录态token
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 调用微信jscode2session接口获取openid和session_key
     * @param appid 小程序appid
     * @param secret 小程序appsecret
     * @param code 登录时获取的code
     * @return 微信返回的结果，包含openid和session_key
     * @throws Exception 异常信息
     */
    public JSONObject jscode2session(String appid, String secret, String code) throws Exception {
        String url = JSCODE2SESSION_URL + "?appid=" + appid + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        
        // 发送HTTP请求
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        
        // 读取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();
        
        // 解析JSON响应
        return JSONObject.parseObject(response.toString());
    }
    
    /**
     * 解密微信手机号
     * @param encryptedData 加密数据
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的手机号信息
     * @throws Exception 解密异常
     */
    public JSONObject decryptPhoneNumber(String encryptedData, String sessionKey, String iv) throws Exception {
        // Base64解码
        byte[] encryptedDataBytes = Base64.decodeBase64(encryptedData);
        byte[] sessionKeyBytes = Base64.decodeBase64(sessionKey);
        byte[] ivBytes = Base64.decodeBase64(iv);
        
        // 创建解密算法实例
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        
        // 解密
        byte[] decryptedBytes = cipher.doFinal(encryptedDataBytes);
        String decryptedStr = new String(decryptedBytes, StandardCharsets.UTF_8);
        
        // 解析JSON
        return JSONObject.parseObject(decryptedStr);
    }
}