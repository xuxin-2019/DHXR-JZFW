package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Order;
import com.homemaker.entity.OrderVO;
import com.homemaker.mapper.OrderMapper;
import com.homemaker.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单Service实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Override
    public Order createOrder(Order order) {
        // 生成订单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        
        // 设置默认状态为待支付订单
        order.setStatus(0);
        
        // 设置创建时间
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        
        // 保存订单
        save(order);

        return order;
    }
    
    @Override
    public List<Order> findOrdersByUserId(Long userId) {
        return orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
        );
    }
    
    @Override
    public List<Order> findOrdersByNurseId(Long nurseId) {
        return orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .eq("nurse_id", nurseId)
                        .orderByDesc("create_time")
        );
    }
    
    @Override
    public List<Order> findAllOrders(Integer status) {
        if (status != null) {
            return orderMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                            .eq("status", status)
                            .orderByDesc("create_time")
            );
        } else {
            return orderMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                            .orderByDesc("create_time")
            );
        }
    }
    
    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUpdateTime(new Date());
        
        return updateById(order);
    }
    
    @Override
    public boolean assignOrderToNurse(Long orderId, Long nurseId) {
        Order order = getById(orderId);
        // 允许待派单(1)、已派单(2)、已拒绝(7)状态的订单分配护工
        if (order != null && (order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 7)) {
            order.setNurseId(nurseId);
            order.setStatus(2); // 更新状态为已派单
            // 创建日期格式化对象
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            order.setServiceTime(dateFormat.format(new Date()));
            order.setUpdateTime(new Date());
            
            return updateById(order);
        }
        return false;
    }
    
    /**
     * 生成订单编号
     * @return 订单编号
     */
    private String generateOrderNo() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "ORDER" + uuid.substring(0, 16).toUpperCase();
    }
    
    @Override
    public boolean editOrder(Order order) {
        // 获取现有订单
        Order existingOrder = getById(order.getId());
        if (existingOrder != null) {
            // 更新允许修改的字段：服务类型、服务时间、开始时间、结束时间、服务地址和服务时长
            existingOrder.setServiceTypeId(order.getServiceTypeId());
            existingOrder.setServiceTime(order.getServiceTime());
            existingOrder.setStartTime(order.getStartTime());
            existingOrder.setEndTime(order.getEndTime());
            existingOrder.setServiceAddress(order.getServiceAddress());
            // 如果传入了服务时长，则更新
            if (order.getServiceDuration() != null) {
                existingOrder.setServiceDuration(order.getServiceDuration());
            }
            existingOrder.setUpdateTime(new Date());
            
            return updateById(existingOrder);
        }
        return false;
    }
    
    @Override
    public boolean deleteOrder(Long id) {
        // 先查询订单的当前状态
        Order existingOrder = getById(id);
        if (existingOrder == null) {
            return false;
        }
        
        // 检查订单状态，如果是已拒绝（7），则不允许删除
        if (existingOrder.getStatus() == 7) {
            return false;
        }
        
        // 逻辑删除：将状态改为已取消（6），而不是真正从数据库删除
        Order order = new Order();
        order.setId(id);
        order.setStatus(6); // 6表示已取消
        order.setUpdateTime(new Date());
        
        return updateById(order);
    }
    
    @Override
    public IPage<OrderVO> findOrdersWithDetailsPage(Page<OrderVO> page, Map<String, Object> params) {
        return orderMapper.selectOrderListWithDetails(page, params);
    }
    
    @Override
    public boolean hasNurseAcceptedOrderInTimeRange(Long nurseId, String startTime, String endTime) {
        // 查询护工的所有已接单、服务中和已完成状态的订单
        // 这些状态的订单表示护工在相应时间段内有工作安排
        List<Order> orders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .eq("nurse_id", nurseId)
                        .in("status", 3) // 3:已接单
        );
        
        // 检查是否有订单与指定时间范围重叠
        for (Order order : orders) {
            // 确保订单有开始时间和结束时间
            if (order.getStartTime() != null && order.getEndTime() != null) {
                // 检查时间重叠
                // 时间重叠的条件：订单的开始时间 <= 查询的结束时间 且 订单的结束时间 >= 查询的开始时间
                if ((order.getStartTime().compareTo(endTime) <= 0) && (order.getEndTime().compareTo(startTime) >= 0)) {
                    return true; // 有时间重叠的订单
                }
            }
        }
        
        return false; // 没有时间重叠的订单
    }
    
    @Override
    public List<Order> findPendingPaymentOrders() {
        // 查询所有待支付状态的订单（状态为0）
        return orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>()
                        .eq("status", 0) // 0:待支付
                        .orderByAsc("create_time") // 按创建时间升序排列，优先处理更早的订单
        );
    }
}