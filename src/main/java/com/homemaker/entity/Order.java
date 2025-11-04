package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
@Data
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
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
     * 护工ID
     */
    private Long nurseId;

    /**
     * 服务类型ID
     */
    private Long serviceTypeId;

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

}