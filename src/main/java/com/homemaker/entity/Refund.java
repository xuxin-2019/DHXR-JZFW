package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退款申请表
 */
@Data
@TableName("t_refund")
public class Refund {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付记录ID
     */
    private Long paymentId;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 微信支付交易号
     */
    private String transactionId;

    /**
     * 商户退款单号
     */
    private String outRefundNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款状态 1:申请中 2:已通过 3:已拒绝 4:退款中 5:退款成功 6:退款失败
     */
    private Integer status;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 审核备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}