package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.Order;
import com.homemaker.entity.Payment;
import com.homemaker.service.OrderService;
import com.homemaker.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付Controller
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付管理")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    /**
     * 创建支付并获取微信支付参数
     * @param orderId 订单ID
     * @param openid 用户openid
     * @return 支付参数
     */
    @PostMapping("/create/{orderId}")
    @Operation(summary = "创建支付并获取微信支付参数")
    public Result createPayment(@PathVariable Long orderId, @RequestBody Map<String, String> requestBody) {
        String openid = requestBody.get("openid");
        try {
            // 查询订单
            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != 0) {
                return Result.error("订单状态不允许支付");
            }

            // 查询是否已有支付记录
            Payment existingPayment = paymentService.getPaymentByOrderId(orderId);
            Payment payment;
            
            if (existingPayment != null) {
                payment = existingPayment;
                // 如果是已关闭的订单，或者支付中的订单，创建新的支付记录
                if (payment.getStatus() == 4 || payment.getStatus() == 2) {
                    // 删除已存在的支付记录
                    paymentService.removeById(payment.getId());
                    // 创建新的支付记录
                    payment = paymentService.createPayment(orderId, order.getOrderNo(), order.getTotalAmount(), order.getUserId());
                }
            } else {
                // 创建新的支付记录
                payment = paymentService.createPayment(orderId, order.getOrderNo(), order.getTotalAmount(), order.getUserId());
            }

            // 生成微信支付参数
            Map<String, Object> payParams = paymentService.generateWxPayParams(payment, openid);
            if (payParams == null) {
                return Result.error("生成支付参数失败");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", payment.getId());
            result.put("outTradeNo", payment.getOutTradeNo());
            result.put("payParams", payParams);
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建支付失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付状态
     * @param orderId 订单ID
     * @return 支付状态
     */
    @GetMapping("/status")
    @Operation(summary = "查询支付状态")
    public Result queryPayStatus(@RequestParam Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            // 重新查询微信支付状态
            Integer status = paymentService.queryPayStatus(payment.getOutTradeNo());
            
            // 查询订单信息
            Order order = orderService.getById(orderId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("status", status);
            result.put("statusText", getStatusText(status));
            
            // 添加订单信息
            if (order != null) {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", order.getId());
                orderInfo.put("orderNo", order.getOrderNo());
                orderInfo.put("totalAmount", order.getTotalAmount());
                orderInfo.put("serviceTypeId", order.getServiceTypeId());
                orderInfo.put("serviceAddress", order.getServiceAddress());
                orderInfo.put("serviceTime", order.getServiceTime());
                orderInfo.put("serviceDuration", order.getServiceDuration());
                result.put("orderInfo", orderInfo);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询支付状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新支付状态
     * 用于处理不同支付失败场景的状态更新
     * @param requestData 请求数据
     * @return 操作结果
     */
    @PostMapping("/updateStatus")
    @Operation(summary = "更新支付状态")
    public Result updatePaymentStatus(@RequestBody Map<String, Object> requestData) {
        Long orderId = null;
        Integer status = null;
        
        try {
            // 解析请求参数
            if (requestData.containsKey("orderId")) {
                orderId = Long.valueOf(requestData.get("orderId").toString());
            }
            if (requestData.containsKey("status")) {
                status = Integer.valueOf(requestData.get("status").toString());
            }
            
            // 参数校验
            if (orderId == null || status == null) {
                return Result.error("订单ID和状态不能为空");
            }
            
            // 检查状态值是否有效
            if (status < 1 || status > 4) {
                return Result.error("无效的状态值，状态值范围：1-4");
            }
            
            // 查询订单是否存在
            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 查询支付记录
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }
            
            // 更新支付状态
            boolean updated = paymentService.updatePaymentStatus(payment, status);
            
            if (updated) {
                logger.info("更新支付状态成功，订单ID: {}, 新状态: {}", orderId, getStatusText(status));
                Map<String, Object> result = new HashMap<>();
                result.put("orderId", orderId);
                result.put("status", status);
                result.put("statusText", getStatusText(status));
                return Result.success(result);
            } else {
                logger.error("更新支付状态失败，订单ID: {}", orderId);
                return Result.error("更新支付状态失败");
            }
        } catch (NumberFormatException e) {
            return Result.error("参数格式错误");
        } catch (Exception e) {
            logger.error("更新支付状态异常，订单ID: {}", orderId, e);
            return Result.error("更新支付状态异常: " + e.getMessage());
        }
    }

    /**
     * 关闭支付订单
     * 包括关闭微信支付订单和更新本地订单状态
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/close/{orderId}")
    @Operation(summary = "关闭支付订单")
    public Result closePayOrder(@PathVariable Long orderId) {
        logger.info("开始处理关闭支付订单请求，订单ID: {}", orderId);
        
        try {
            // 1. 查询订单是否存在
            Order order = orderService.getById(orderId);
            if (order == null) {
                logger.warn("订单不存在，订单ID: {}", orderId);
                return Result.error("订单不存在");
            }
            
            // 2. 查询支付记录
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                logger.warn("支付记录不存在，订单ID: {}", orderId);
                return Result.error("支付记录不存在");
            }
            
            // 3. 检查支付状态是否可关闭（只有待支付状态可以关闭）
            if (payment.getStatus() != 1) {
                String statusText = getStatusText(payment.getStatus());
                logger.warn("订单状态不允许关闭，当前状态: {}, 订单ID: {}", statusText, orderId);
                return Result.error("当前订单状态为" + statusText + "，不允许关闭");
            }
            
            // 4. 执行关闭操作
            boolean closed = paymentService.closePayOrder(payment.getOutTradeNo());
            
            // 5. 返回结果
            if (closed) {
                logger.info("关闭支付订单成功，订单ID: {}, 商户订单号: {}", 
                          orderId, payment.getOutTradeNo());
                
                // 构建返回数据
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("orderId", orderId);
                resultData.put("outTradeNo", payment.getOutTradeNo());
                resultData.put("status", "closed");
                resultData.put("message", "支付订单已关闭");
                
                return Result.success(resultData);
            } else {
                logger.error("关闭支付订单失败，订单ID: {}, 商户订单号: {}", 
                           orderId, payment.getOutTradeNo());
                return Result.error("关闭支付订单失败，请稍后重试");
            }
        } catch (Exception e) {
            logger.error("关闭支付订单过程中发生异常，订单ID: {}", orderId, e);
            // 记录详细错误信息，但返回友好的错误提示
            return Result.error("系统处理异常，请稍后重试");
        }
    }

    /**
     * 微信支付回调接口
     * 处理微信支付异步通知，更新支付状态和订单状态
     * @param request 请求
     * @param response 响应
     */
    @PostMapping("/notify")
    @Operation(summary = "微信支付回调")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 读取回调数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder notifyData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                notifyData.append(line);
            }
            reader.close();

            // 处理回调
            boolean handled = paymentService.handlePayNotify(notifyData.toString());

            // 返回结果给微信服务器
            response.setContentType("application/xml");
            if (handled) {
                response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
            } else {
                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>");
            }
        } catch (Exception e) {
            logger.error("处理微信支付回调异常", e);
            try {
                // 发生异常时也需要返回错误结果给微信服务器
                response.setContentType("application/xml");
                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[服务器异常]]></return_msg></xml>");
            } catch (Exception ex) {
                logger.error("返回微信支付回调响应异常", ex);
            }
        }
    }
    
    /**
     * 支付结果跳转页面
     * 用于微信支付完成后跳转到支付结果展示页面
     * @param orderId 订单ID
     * @param status 支付状态（1-待支付，3-支付成功，4-支付失败）
     * @param response 响应对象
     * @return 支付结果信息
     * @throws Exception 异常信息
     */
    @GetMapping("/result")
    @Operation(summary = "支付结果页面")
    public Map<String, Object> paymentResult(@RequestParam Long orderId, @RequestParam Integer status) throws Exception {
        // 构建返回给前端的支付结果信息
        // 由于前端使用微信小程序的页面跳转，我们只需要提供正确的支付状态信息
        // 将数字状态码转换为前端使用的字符串状态
        String statusStr = "fail"; // 默认失败状态
        if (status == 3) {
            statusStr = "success"; // 3表示支付成功
        } else if (status == 1) {
            statusStr = "pending"; // 1表示待支付
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", statusStr);
        
        return result;
    }

    /**
     * 获取支付状态文本
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待支付";
            case 2:
                return "支付中";
            case 3:
                return "支付成功";
            case 4:
                return "支付失败";
            default:
                return "未知状态";
        }
    }
}