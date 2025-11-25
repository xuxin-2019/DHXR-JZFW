package com.homemaker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Order;
import com.homemaker.entity.OrderVO;

import java.util.List;
import java.util.Map;

/**
 * 订单Service接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     * @param order 订单信息
     * @return 是否创建成功
     */
    Order createOrder(Order order);
    
    /**
     * 查询用户的订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> findOrdersByUserId(Long userId);
    
    /**
     * 查询护工的订单列表
     * @param nurseId 护工ID
     * @return 订单列表
     */
    List<Order> findOrdersByNurseId(Long nurseId);
    
    /**
     * 查询所有订单
     * @param status 订单状态(可选)
     * @return 订单列表
     */
    List<Order> findAllOrders(Integer status);
    
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 订单状态
     * @return 是否更新成功
     */
    boolean updateOrderStatus(Long id, Integer status);
    
    /**
     * 分配订单给护工
     * @param orderId 订单ID
     * @param nurseId 护工ID
     * @return 是否分配成功
     */
    boolean assignOrderToNurse(Long orderId, Long nurseId);
    
    /**
     * 编辑订单
     * @param order 订单信息，仅更新服务类型、服务时间和服务地址
     * @return 是否编辑成功
     */
    boolean editOrder(Order order);
    
    /**
     * 逻辑删除订单
     * @param id 订单ID
     * @return 是否删除成功
     */
    boolean deleteOrder(Long id);
    
    /**
     * 分页查询订单列表，支持联表查询用户、护工和服务类型信息
     * @param page 分页对象
     * @param params 查询参数
     * @return 订单VO分页结果
     */
    IPage<OrderVO> findOrdersWithDetailsPage(Page<OrderVO> page, Map<String, Object> params);
    
    /**
     * 检查护工在指定时间范围内是否有已接单状态的订单
     * @param nurseId 护工ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 如果有已接单状态的订单则返回true，否则返回false
     */
    boolean hasNurseAcceptedOrderInTimeRange(Long nurseId, String startTime, String endTime);
    
    /**
     * 查询所有待支付的订单
     * @return 待支付订单列表
     */
    List<Order> findPendingPaymentOrders();
}