package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.OrderVO;
import com.homemaker.entity.Order;
import com.homemaker.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单相关的接口")
public class OrderController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param order 订单信息，包含服务时长（serviceDuration）字段
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新的订单，支持传入serviceDuration（服务时长，单位：分钟）")
    public Result createOrder(@RequestBody Order order) {
        Order result = orderService.createOrder(order);
        return Result.success("创建成功",result);
    }

    /**
     * 查询用户订单
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/user")
    @Operation(summary = "查询用户订单", description = "根据用户ID查询订单列表")
    public Result findOrdersByUserId(@RequestParam Long userId) {
        List<Order> orders = orderService.findOrdersByUserId(userId);
        return Result.success("查询成功", orders);
    }

    /**
     * 查询护工订单
     * @param nurseId 护工ID
     * @return 订单列表
     */
    @GetMapping("/nurse")
    @Operation(summary = "查询护工订单", description = "根据护工ID查询订单列表")
    public Result findOrdersByNurseId(@RequestParam Long nurseId) {
        List<Order> orders = orderService.findOrdersByNurseId(nurseId);
        return Result.success("查询成功", orders);
    }

    /**
     * 查询所有订单（分页），支持联表查询用户、护工和服务类型信息
     * @param page 页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param status 订单状态（可选，null表示查询所有）
     * @param orderNo 订单编号（可选，用于搜索）
     * @param username 用户名（可选，用于搜索）
     * @param nurseName 护工名（可选，用于搜索）
     * @param serviceTypeId 服务类型ID（可选，用于筛选）
     * @return 分页订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有订单", description = "查询所有订单列表，支持联表查询用户、护工和服务类型信息")
    public Result findAllOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nurseName,
            @RequestParam(required = false) Integer serviceTypeId) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderVO> pageResult = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        
        // 构造查询参数
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        
        // 设置查询参数
        if (status != null) {
            params.put("status", status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            params.put("orderNo", orderNo);
        }
        if (username != null && !username.isEmpty()) {
            params.put("username", username);
        }
        if (nurseName != null && !nurseName.isEmpty()) {
            params.put("nurseName", nurseName);
        }
        if (serviceTypeId != null) {
            params.put("serviceTypeId", serviceTypeId);
        }
        
        // 执行分页查询，使用联表查询方法
        com.baomidou.mybatisplus.core.metadata.IPage<OrderVO> result = 
            orderService.findOrdersWithDetailsPage(pageResult, params);
        
        // 构造分页结果
        java.util.Map<String, Object> pageData = new java.util.HashMap<>();
        pageData.put("records", result.getRecords());
        pageData.put("total", result.getTotal());
        pageData.put("pageSize", result.getSize());
        pageData.put("current", result.getCurrent());
        
        return Result.success("查询成功", pageData);
    }

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 订单状态
     * @return 更新结果
     */
    @PostMapping("/status")
    @Operation(summary = "更新订单状态", description = "更新指定订单的状态")
    public Result updateOrderStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean result = orderService.updateOrderStatus(id, status);
        if (result) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 分配订单给护工
     * @param orderId 订单ID
     * @param nurseId 护工ID
     * @return 分配结果
     */
    @PostMapping("/assign")
    @Operation(summary = "分配订单给护工", description = "将订单分配给指定护工")
    public Result assignOrderToNurse(@RequestParam Long orderId, @RequestParam Long nurseId) {
        boolean result = orderService.assignOrderToNurse(orderId, nurseId);
        if (result) {
            return Result.success("分配成功");
        } else {
            return Result.error("分配失败");
        }
    }
    
    /**
     * 编辑订单
     * @param order 订单信息，包含要更新的字段
     * @return 编辑结果
     */
    @PostMapping("/update")
    @Operation(summary = "编辑订单", description = "编辑订单的服务类型、服务时间和服务地址")
    public Result editOrder(@RequestBody Order order) {
        boolean result = orderService.editOrder(order);
        if (result) {
            return Result.success("编辑成功");
        } else {
            return Result.error("编辑失败");
        }
    }
    
    /**
     * 删除订单（逻辑删除）
     * @param id 订单ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "删除订单", description = "逻辑删除订单，将状态改为已取消")
    public Result deleteOrder(@RequestParam Long id) {
        boolean result = orderService.deleteOrder(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 微信小程序端查询订单列表
     * @param page 页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param userId 用户ID（必填）
     * @param status 订单状态字符串（可选，格式如"[1,2,7]"）
     * @return 分页订单列表
     */
    @GetMapping("/wxList")
    @Operation(summary = "微信小程序端查询订单", description = "根据用户ID和状态数组查询订单列表，支持分页")
    public Result findOrdersByUserIdAndStatus(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long nurseId,
            @RequestParam(required = false) String status) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderVO> pageResult = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        
        // 构造查询参数
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        
        // 设置查询参数
        params.put("userId", userId);
        params.put("nurseId", nurseId);

        // 解析状态字符串为列表
        if (status != null && !status.isEmpty()) {
            // 处理格式如"[1,2,7]"的字符串
            try {
                // 移除方括号并分割
                String cleanStatus = status.replaceAll("\\[|\\]", "");
                if (!cleanStatus.isEmpty()) {
                    String[] statusArray = cleanStatus.split(",");
                    String statusJoin = StringUtils.join(statusArray, ",");
                    params.put("statusList", "(" + statusJoin + ")");
                }
            } catch (Exception e) {
                // 如果解析失败，记录错误但不影响查询
                log.warn("解析订单状态参数失败: {}", status, e);
            }
        }
        
        // 执行分页查询，使用联表查询方法
        com.baomidou.mybatisplus.core.metadata.IPage<OrderVO> result = 
            orderService.findOrdersWithDetailsPage(pageResult, params);
        
        // 构造分页结果
        java.util.Map<String, Object> pageData = new java.util.HashMap<>();
        pageData.put("records", result.getRecords());
        pageData.put("total", result.getTotal());
        pageData.put("pageSize", result.getSize());
        pageData.put("current", result.getCurrent());
        
        return Result.success("查询成功", pageData);
    }
}