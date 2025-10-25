package com.homemaker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务类型实体类
 */
@Data
@TableName("service_type")
public class ServiceType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务类型ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 服务价格
     */
    private BigDecimal price;

    /**
     * 服务时长(分钟)
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}