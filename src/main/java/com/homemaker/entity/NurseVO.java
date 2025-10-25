package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 护工VO类，包含服务类型名称
 */
@Data
public class NurseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 护工ID
     */
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 服务类型ID
     */
    private Long serviceTypeId;

    /**
     * 服务类型名称
     */
    private String serviceTypeName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 状态(1:空闲, 2:忙碌, 3:离线)
     */
    private Integer status;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 服务次数
     */
    private Integer serviceCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}