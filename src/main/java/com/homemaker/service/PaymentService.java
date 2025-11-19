package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付Service接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付记录
     * @param orderId 订单ID
     * @param orderNo 订单编号
     * @param amount 支付金额
     * @param userId 用户ID
     * @return 支付记录
     */
    Payment createPayment(Long orderId, String orderNo, BigDecimal amount, Long userId);

    /**
     * 生成微信支付参数
     * @param payment 支付记录
     * @param openid 用户openid
     * @return 支付参数
     */
    Map<String, Object> generateWxPayParams(Payment payment, String openid);

    /**
     * 根据订单ID查询支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payment getPaymentByOrderId(Long orderId);

    /**
     * 根据商户订单号查询支付记录
     * @param outTradeNo 商户订单号
     * @return 支付记录
     */
    Payment getPaymentByOutTradeNo(String outTradeNo);

    /**
     * 根据微信支付交易号查询支付记录
     * @param transactionId 微信支付交易号
     * @return 支付记录
     */
    Payment getPaymentByTransactionId(String transactionId);

    /**
     * 更新支付状态
     * @param payment 支付记录
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updatePaymentStatus(Payment payment, Integer status);

    /**
     * 处理支付回调
     * @param notifyData 回调数据
     * @return 处理结果
     */
    boolean handlePayNotify(String notifyData);

    /**
     * 查询订单支付状态
     * @param outTradeNo 商户订单号
     * @return 支付状态
     */
    Integer queryPayStatus(String outTradeNo);

    /**
     * 关闭支付订单
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     */
    boolean closePayOrder(String outTradeNo);
}