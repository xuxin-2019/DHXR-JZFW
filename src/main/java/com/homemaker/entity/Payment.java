package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付记录实体类
 */
@Data
@TableName("payment_record")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败
     */
    private Integer status;

    /**
     * 微信支付交易号
     */
    private String transactionId;

    /**
     * 微信支付商户订单号
     */
    private String outTradeNo;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 支付方式：1-微信支付
     */
    private Integer payType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付回调结果
     */
    private String callbackResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}