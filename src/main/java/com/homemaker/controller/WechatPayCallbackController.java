package com.homemaker.controller;

import com.homemaker.service.PaymentService;
import com.homemaker.service.RefundService;
import com.homemaker.util.WechatPayUtil;
import java.io.BufferedReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 微信支付回调控制器
 * 处理微信支付和退款的异步通知
 */
@RestController
@RequestMapping("/api/wechat/callback")
public class WechatPayCallbackController {

    @Autowired
    private WechatPayUtil wechatPayUtil;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RefundService refundService;

    /**
     * 微信支付结果通知回调
     * @param request HTTP请求
     * @return XML格式的响应
     */
    @PostMapping("/pay")
    public String handlePayCallback(HttpServletRequest request) {
        try {
            // 1. 获取请求的XML数据
            StringBuilder xmlBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(request.getInputStream(), "UTF-8"))) {
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }
            String xmlData = xmlBuilder.toString();
            System.out.println("微信支付回调通知XML数据: " + xmlData);

            // 2. 将XML转换为Map
            Map<String, String> params = wechatPayUtil.xmlToMap(xmlData);

            // 3. 验证签名
            if (!wechatPayUtil.verifySign(params)) {
                System.out.println("微信支付回调签名验证失败");
                return generateFailXml("签名验证失败");
            }

            // 4. 检查通信状态
            if (!"SUCCESS".equals(params.get("return_code"))) {
                String errorMsg = params.get("return_msg") != null ? params.get("return_msg") : "未知错误";
                System.out.println("微信支付回调通信失败: " + errorMsg);
                return generateFailXml(errorMsg);
            }

            // 5. 检查业务状态
            if (!"SUCCESS".equals(params.get("result_code"))) {
                String errorCode = params.get("err_code") != null ? params.get("err_code") : "未知错误码";
                String errorMsg = params.get("err_code_des") != null ? params.get("err_code_des") : "未知错误";
                System.out.println("微信支付业务失败: " + errorCode + " - " + errorMsg);
                return generateFailXml(errorMsg);
            }

            // 6. 处理支付通知
            boolean result = paymentService.handlePayNotify(params);
            if (!result) {
                System.out.println("微信支付回调处理失败");
                return generateFailXml("处理失败");
            }

            // 7. 返回成功响应
            return generateSuccessXml();
        } catch (Exception e) {
            e.printStackTrace();
            return generateFailXml("服务器内部错误");
        }
    }

    /**
     * 微信退款结果通知回调
     * @param request HTTP请求
     * @return XML格式的响应
     */
    @PostMapping("/refund")
    public String handleRefundCallback(HttpServletRequest request) {
        try {
            // 1. 获取请求的XML数据
            StringBuilder xmlBuilder = new StringBuilder();
            String line;
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(request.getInputStream(), "UTF-8"))) {
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }
            String xmlData = xmlBuilder.toString();
            System.out.println("微信退款回调通知XML数据: " + xmlData);

            // 2. 将XML转换为Map
            Map<String, String> params = wechatPayUtil.xmlToMap(xmlData);

            // 3. 验证签名
            if (!wechatPayUtil.verifySign(params)) {
                System.out.println("微信退款回调签名验证失败");
                return generateFailXml("签名验证失败");
            }

            // 4. 检查通信状态
            if (!"SUCCESS".equals(params.get("return_code"))) {
                String errorMsg = params.get("return_msg") != null ? params.get("return_msg") : "未知错误";
                System.out.println("微信退款回调通信失败: " + errorMsg);
                return generateFailXml(errorMsg);
            }

            // 5. 处理退款通知
            boolean result = refundService.handleRefundNotify(params);
            if (!result) {
                System.out.println("微信退款回调处理失败");
                return generateFailXml("处理失败");
            }

            // 6. 返回成功响应
            return generateSuccessXml();
        } catch (Exception e) {
            e.printStackTrace();
            return generateFailXml("服务器内部错误");
        }
    }

    /**
     * 生成成功的XML响应
     * @return 成功XML
     */
    private String generateSuccessXml() {
        return "<xml>\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
    }

    /**
     * 生成失败的XML响应
     * @param message 失败消息
     * @return 失败XML
     */
    private String generateFailXml(String message) {
        return "<xml>\n" +
                "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                "  <return_msg><![CDATA[" + message + "]]></return_msg>\n" +
                "</xml>";
    }
}