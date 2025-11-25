package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.homemaker.config.WxPayConfigImpl;
import com.homemaker.entity.Payment;
import com.homemaker.mapper.PaymentMapper;
import com.homemaker.service.OrderService;
import com.homemaker.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.NetworkInterface;
import java.util.*;

/**
 * 支付Service实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    // 微信支付环境配置
    @Value("${wechat.pay.isProduction}")
    private boolean isProduction;
    
    @Value("${wechat.pay.notify_url}")
    private String notifyUrl;
    
    @Value("${wechat.pay.appid}")
    private String appid;
    
    @Value("${wechat.pay.mchid}")
    private String mchid;
    
    @Value("${wechat.pay.key}")
    private String key;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private WxPayConfigImpl wxPayConfig;

    @Override
    public Payment createPayment(Long orderId, String orderNo, BigDecimal amount, Long userId) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setOrderNo(orderNo);
        payment.setAmount(amount);
        payment.setUserId(userId);
        payment.setStatus(1); // 待支付
        payment.setPayType(1); // 微信支付
        payment.setOutTradeNo(generateOutTradeNo());
        payment.setCreateTime(new Date());
       payment.setUpdateTime(new Date());
        
        save(payment);
        return payment;
    }

    @Override
    public Map<String, Object> generateWxPayParams(Payment payment, String openid) {
        Map<String, Object> params = new HashMap<>();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        int totalFee = payment.getAmount().multiply(new BigDecimal(100)).intValue();
        String prepayId = null;
        
        try {
            if (isProduction) {
                // 生产环境：调用真实的微信支付统一下单接口
                prepayId = callWechatPayUnifiedOrder(payment, openid, totalFee);
                logger.info("成功获取微信支付prepay_id: {}", prepayId);
            } else {
                // 测试环境：使用模拟的prepay_id
                prepayId = "wx" + timestamp.substring(2) + "T123456";
                logger.info("测试环境，使用模拟prepay_id: {}", prepayId);
            }
            
            // 构造微信小程序支付所需的五个参数
            String packageValue = "prepay_id=" + prepayId;
            String signType = "MD5";
            
            // 生成签名
            String paySign;
            if (isProduction) {
                // 生产环境：使用微信SDK生成标准签名
                paySign = generateWechatPaySign(appid, timestamp, nonceStr, packageValue, signType);
            } else {
                // 测试环境：使用模拟签名
                paySign = generateMockPaySign(timestamp, nonceStr, prepayId, totalFee);
            }
            
            // 设置返回参数
            params.put("timeStamp", timestamp);
            params.put("nonceStr", nonceStr);
            params.put("package", packageValue);
            params.put("signType", signType);
            params.put("paySign", paySign);
            
        } catch (Exception e) {
            logger.error("生成微信支付参数失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成微信支付参数失败", e);
        }
        
        return params;
    }
    
    /**
     * 获取本地IP地址
     * @return 本地IP地址字符串
     */
    private String getLocalIpAddress() {
        try {
            // 获取本地主机
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            if (networkInterfaces != null) {
                while (networkInterfaces.hasMoreElements()) {
                    java.net.NetworkInterface networkInterface = networkInterfaces.nextElement();
                    // 跳过虚拟接口和未启用的接口
                    if (networkInterface.isVirtual() || !networkInterface.isUp()) {
                        continue;
                    }
                    
                    // 获取该接口的所有IP地址
                    java.util.Enumeration<java.net.InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        java.net.InetAddress inetAddress = inetAddresses.nextElement();
                        // 过滤掉回环地址和IPv6地址
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            
            // 如果没有找到合适的IP，返回本地主机地址
            return localHost.getHostAddress();
        } catch (Exception e) {
            logger.error("获取本地IP地址失败: {}", e.getMessage(), e);
            // 出错时返回默认IP
            return "127.0.0.1";
        }
    }
    
    /**
     * 调用微信支付统一下单接口
     * @param payment 支付记录
     * @param openid 用户openid
     * @param totalFee 总金额（分）
     * @return prepay_id
     * @throws Exception 异常信息
     */
    private String callWechatPayUnifiedOrder(Payment payment, String openid, int totalFee) throws Exception {
         // 实现微信支付统一下单接口调用
         WXPay wxpay = new WXPay(wxPayConfig, WXPayConstants.SignType.MD5);   
        // 构建统一下单参数
        Map<String, String> data = new HashMap<>();
        data.put("appid", appid);
        data.put("mch_id", mchid);
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        data.put("body", "家政服务订单支付");
        data.put("out_trade_no", payment.getOutTradeNo());
        data.put("total_fee", String.valueOf(totalFee));
        String localIp = getLocalIpAddress();
        data.put("spbill_create_ip", localIp);
        logger.info("使用本地IP地址: {} 进行微信支付统一下单", localIp);
        data.put("notify_url", notifyUrl);
        data.put("trade_type", "JSAPI");
        data.put("openid", openid);
        
        // 生成签名
        String sign = WXPayUtil.generateSignature(data, key, WXPayConstants.SignType.MD5);
        data.put("sign", sign);
        
        // 调用统一下单接口
        Map<String, String> resp = wxpay.unifiedOrder(data);
        
        // 检查返回结果
        if ("SUCCESS".equals(resp.get("return_code")) && "SUCCESS".equals(resp.get("result_code"))) {
            return resp.get("prepay_id");
        } else {
            String errorMsg = resp.get("err_code_des") != null ? resp.get("err_code_des") : resp.get("return_msg");
            throw new RuntimeException("微信支付统一下单失败: " + errorMsg);
        }
    }
    
    /**
     * 生成微信支付签名
     * @param appid 应用ID
     * @param timestamp 时间戳
     * @param nonceStr 随机字符串
     * @param packageValue 统一下单接口返回的prepay_id参数值
     * @param signType 签名类型
     * @return 签名
     * @throws Exception 异常信息
     */
    private String generateWechatPaySign(String appid, String timestamp, String nonceStr, 
                                        String packageValue, String signType) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("appId", appid);
        data.put("timeStamp", timestamp);
        data.put("nonceStr", nonceStr);
        data.put("package", packageValue);
        data.put("signType", signType);
        
        return WXPayUtil.generateSignature(data, key, WXPayConstants.SignType.MD5);
    }
    
    /**
     * 生成模拟的支付签名（仅测试环境使用）
     * @param timestamp 时间戳
     * @param nonceStr 随机字符串
     * @param prepayId 预支付ID
     * @param totalFee 总金额（分）
     * @return 模拟签名
     */
    private String generateMockPaySign(String timestamp, String nonceStr, String prepayId, int totalFee) {
        // 简单的签名生成，仅用于测试，包含total_fee信息
        String rawString = timestamp + nonceStr + prepayId + totalFee;
        // 返回一个基于输入参数的哈希值作为模拟签名
        return Integer.toHexString(rawString.hashCode()).toUpperCase();
    }
    


    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentMapper.selectByOrderId(orderId);
    }

    @Override
    public Payment getPaymentByOutTradeNo(String outTradeNo) {
        return paymentMapper.selectByOutTradeNo(outTradeNo);
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentMapper.selectByTransactionId(transactionId);
    }

    @Override
    public boolean updatePaymentStatus(Payment payment, Integer status) {
        payment.setStatus(status);
        payment.setUpdateTime(new Date());
        if (status == 3) { // 支付成功
            payment.setPayTime(new Date());
        }
        return updateById(payment);
    }

    @Autowired
    private OrderService orderService;

    @Override
    public boolean handlePayNotify(String notifyData) {
        try {
            // 解析微信支付回调数据
            logger.info("收到微信支付回调通知: {}", notifyData);
            
            // 将XML格式的回调数据转换为Map
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);
            
            // 验证回调签名
            boolean signatureValid = WXPayUtil.isSignatureValid(notifyMap, key, WXPayConstants.SignType.MD5);
            if (!signatureValid) {
                logger.error("微信支付回调签名验证失败");
                return false;
            }
            
            // 检查返回状态
            if (!"SUCCESS".equals(notifyMap.get("return_code")) || !"SUCCESS".equals(notifyMap.get("result_code"))) {
                logger.error("微信支付回调返回失败: {}", notifyMap.get("return_msg"));
                return false;
            }
            
            // 获取商户订单号和微信支付交易号
            String outTradeNo = notifyMap.get("out_trade_no");
            String transactionId = notifyMap.get("transaction_id");
            
            // 查询支付记录
            Payment payment = getPaymentByOutTradeNo(outTradeNo);
            if (payment == null) {
                logger.error("未找到对应的支付记录: {}", outTradeNo);
                return false;
            }
            
            // 检查支付状态，避免重复处理
            if (payment.getStatus() == 3) { // 3: 支付成功
                logger.warn("支付记录已处理过: {}", outTradeNo);
                return true; // 返回true避免微信重复通知
            }
            
            // 更新支付记录
            payment.setStatus(3); // 3: 支付成功
            payment.setTransactionId(transactionId);
            payment.setPayTime(new Date());
            payment.setCallbackResult(notifyData);
            payment.setUpdateTime(new Date());
            
            if (!updateById(payment)) {
                logger.error("更新支付记录失败: {}", outTradeNo);
                return false;
            }
            
            // 更新订单状态为待派单（支付成功后订单状态变为待派单）
            if (!orderService.updateOrderStatus(payment.getOrderId(), 1)) {
                logger.error("更新订单状态失败，订单ID: {}", payment.getOrderId());
                // 记录日志，但不影响回调结果，避免微信重复通知
            }
            
            logger.info("微信支付回调处理成功，订单号: {}, 交易号: {}", outTradeNo, transactionId);
            return true;
        } catch (Exception e) {
            logger.error("处理微信支付回调异常", e);
            return false;
        }
    }

    @Override
    public Integer queryPayStatus(String outTradeNo) {
        // 这里将在WechatPayUtil实现后调用相应方法
        // 暂时返回待支付状态
        Payment payment = getPaymentByOutTradeNo(outTradeNo);
        return payment != null ? payment.getStatus() : null;
    }

    @Override
    public boolean closePayOrder(String outTradeNo) {
        logger.info("开始关闭支付订单，商户订单号: {}", outTradeNo);

        // 查询支付记录
        Payment payment = getPaymentByOutTradeNo(outTradeNo);
        if (payment == null) {
            logger.error("未找到支付记录，商户订单号: {}", outTradeNo);
            return false;
        }

        // 只有待支付状态的订单才能关闭
        if (payment.getStatus() != 1) {
            logger.warn("订单状态不允许关闭，当前状态: {}, 商户订单号: {}", payment.getStatus(), outTradeNo);
            return false;
        }

        boolean wxPayClosed = true;

        try {
            // 调用微信支付关闭订单接口
            if (isProduction) {
                wxPayClosed = callWechatPayCloseOrder(outTradeNo);
                if (!wxPayClosed) {
                    logger.error("调用微信支付关闭订单接口失败，商户订单号: {}", outTradeNo);
                    // 即使微信支付关闭失败，我们仍然更新本地状态为关闭，避免订单一直处于待支付状态
                }
            } else {
                // 测试环境，模拟成功关闭
                logger.info("测试环境，模拟关闭微信支付订单: {}", outTradeNo);
            }

            // 更新本地支付状态为支付失败
            payment.setStatus(4); // 支付失败
            payment.setUpdateTime(new Date());

            boolean updated = updateById(payment);

            if (updated) {
                // 同步更新订单状态
                boolean orderUpdated = orderService.updateOrderStatus(payment.getOrderId(), 0);
                if (orderUpdated) {
                    logger.info("成功关闭支付订单并更新订单状态，商户订单号: {}, 订单ID: {}",
                            outTradeNo, payment.getOrderId());
                } else {
                    logger.error("关闭支付订单成功，但更新订单状态失败，订单ID: {}", payment.getOrderId());
                    // 记录错误，但不影响返回结果
                }
            } else {
                logger.error("更新支付记录状态失败，商户订单号: {}", outTradeNo);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("关闭支付订单过程中发生异常，商户订单号: {}", outTradeNo, e);
            // 发生异常时，尝试更新本地状态
            try {
                payment.setStatus(4);
                payment.setUpdateTime(new Date());
                updateById(payment);
            } catch (Exception ex) {
                logger.error("异常处理过程中更新支付状态失败，商户订单号: {}", outTradeNo, ex);
            }
            return false;
        }
    }
    
    /**
     * 调用微信支付关闭订单接口
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     * @throws Exception 异常信息
     */
    private boolean callWechatPayCloseOrder(String outTradeNo) throws Exception {
        logger.info("调用微信支付关闭订单接口，商户订单号: {}", outTradeNo);
        
        // 创建微信支付客户端
        WXPay wxpay = new WXPay(wxPayConfig, WXPayConstants.SignType.MD5);
        
        // 构建关闭订单参数
        Map<String, String> data = new HashMap<>();
        data.put("appid", appid);
        data.put("mch_id", mchid);
        data.put("nonce_str", WXPayUtil.generateNonceStr());
        data.put("out_trade_no", outTradeNo);
        
        // 生成签名
        String sign = WXPayUtil.generateSignature(data, key, WXPayConstants.SignType.MD5);
        data.put("sign", sign);
        
        // 调用关闭订单接口
        Map<String, String> resp = wxpay.closeOrder(data);
        
        // 检查返回结果
        if ("SUCCESS".equals(resp.get("return_code")) && "SUCCESS".equals(resp.get("result_code"))) {
            logger.info("微信支付关闭订单成功，商户订单号: {}", outTradeNo);
            return true;
        } else {
            String errorMsg = resp.get("err_code_des") != null ? resp.get("err_code_des") : resp.get("return_msg");
            logger.error("微信支付关闭订单失败: {}, 商户订单号: {}", errorMsg, outTradeNo);
            return false;
        }
    }

    /**
     * 生成商户订单号
     * @return 商户订单号
     */
    private String generateOutTradeNo() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "wxpay_" + timestamp + "_" + uuid.substring(0, 8);
    }
}