package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Payment;
import com.homemaker.mapper.PaymentMapper;
import com.homemaker.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付Service实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

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
        // 测试环境：返回模拟的微信支付参数
        // 注意：在生产环境中，应该调用真实的微信支付接口获取有效的prepay_id
        // 当前使用模拟参数会导致支付超时，这是测试环境的正常现象
        Map<String, Object> params = new HashMap<>();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        
        // 使用基于时间戳的动态prepay_id，使其看起来像是有效的
        String timestampBasedPrepayId = "wx" + timestamp.substring(2) + "T123456";
        
        // 添加微信支付所需的total_fee参数（单位为分）
        // 将BigDecimal金额转换为分
        int totalFee = payment.getAmount().multiply(new BigDecimal(100)).intValue();
        
        params.put("timeStamp", timestamp);
        params.put("nonceStr", nonceStr);
        params.put("package", "prepay_id=" + timestampBasedPrepayId);
        params.put("signType", "MD5");
        params.put("total_fee", totalFee); // 添加缺失的total_fee参数
        
        // 生成基于参数的动态签名，看起来更真实
        String paySign = generateMockPaySign(timestamp, nonceStr, timestampBasedPrepayId);
        params.put("paySign", paySign);
        
        return params;
    }
    
    /**
     * 生成模拟的支付签名
     * 注意：这只是为了测试环境，生产环境应该使用真实的微信支付签名算法
     */
    private String generateMockPaySign(String timestamp, String nonceStr, String prepayId) {
        // 简单的签名生成，仅用于测试
        String rawString = timestamp + nonceStr + prepayId;
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

    @Override
    public boolean handlePayNotify(String notifyData) {
        // 这里将在实现微信支付回调处理逻辑后完善
        return false;
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
        // 这里将在WechatPayUtil实现后调用相应方法
        // 暂时只更新本地状态
        Payment payment = getPaymentByOutTradeNo(outTradeNo);
        if (payment != null && payment.getStatus() == 1) {
            payment.setStatus(4); // 支付失败
            payment.setUpdateTime(new Date());
            return updateById(payment);
        }
        return false;
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