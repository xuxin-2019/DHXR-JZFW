package com.homemaker.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单VO类，用于存储订单联表查询结果
 */
@Data
public class OrderVO {
    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 护工ID
     */
    private Long nurseId;

    /**
     * 护工名称
     */
    private String nurseName;

    /**
     * 服务类型ID
     */
    private Long serviceTypeId;

    /**
     * 服务名称
     */
    private String serviceTypeName;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态(1:待派单, 2:已派单, 3:已接单, 4:服务中, 5:已完成, 6:已取消, 7:已拒绝)
     */
    private Integer status;

    /**
     * 服务地址
     */
    private String serviceAddress;

    /**
     * 服务时间
     */
    private String serviceTime;

    /**
     * 支付时间
     */
    private String paymentTime;

    /**
     * 服务开始时间
     */
    private String startTime;

    /**
     * 服务结束时间
     */
    private String endTime;

    /**
     * 服务时长（单位：分钟）
     */
    private Integer serviceDuration;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户电话
     */
    private String userPhone;

    /**
     * 护工电话
     */
    private String nursePhone;
}