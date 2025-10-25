package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评价实体类
 */
@Data
@TableName("evaluation")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 护工ID
     */
    private Long nurseId;

    /**
     * 评分(1-5星)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

}