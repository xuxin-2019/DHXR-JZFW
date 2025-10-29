package com.homemaker.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评价DTO类，用于联表查询结果展示
 */
@Data
public class EvaluationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 护工ID
     */
    private Long nurseId;

    /**
     * 护工名
     */
    private String nurseName;

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