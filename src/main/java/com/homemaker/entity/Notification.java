package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 护工ID
     */
    private Long nurseId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型(1:订单通知, 2:系统通知)
     */
    private Integer type;

    /**
     * 是否已读(0:未读, 1:已读)
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private Date createTime;

}