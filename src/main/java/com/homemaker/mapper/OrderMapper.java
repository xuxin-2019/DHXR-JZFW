package com.homemaker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.entity.Order;
import com.homemaker.entity.OrderVO;

import java.util.Map;

/**
 * 订单Mapper
 */
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 分页查询订单列表，支持联表查询用户、护工和服务类型信息
     * @param page 分页对象
     * @param params 查询参数
     * @return 订单VO列表
     */
    Page<OrderVO> selectOrderListWithDetails(Page<OrderVO> page, Map<String, Object> params);
}