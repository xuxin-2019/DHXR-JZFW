package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 护工实体类
 */
@Data
@TableName("nurse")
public class Nurse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 护工ID
     */
    @TableId(type = IdType.AUTO)
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

    // 密码字段已移除，系统使用微信登录

    /**
     * 微信openid
     */
    private String openid;

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