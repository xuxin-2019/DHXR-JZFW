package com.homemaker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homemaker.entity.Payment;

/**
 * 支付记录Mapper接口
 */
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 根据订单ID查询支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payment selectByOrderId(Long orderId);

    /**
     * 根据微信支付交易号查询支付记录
     * @param transactionId 微信支付交易号
     * @return 支付记录
     */
    Payment selectByTransactionId(String transactionId);

    /**
     * 根据商户订单号查询支付记录
     * @param outTradeNo 商户订单号
     * @return 支付记录
     */
    Payment selectByOutTradeNo(String outTradeNo);
}