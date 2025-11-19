package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.Order;
import com.homemaker.entity.Payment;
import com.homemaker.service.OrderService;
import com.homemaker.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
                // 如果是已关闭的订单，创建新的支付记录
                if (payment.getStatus() == 4) {
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
    @GetMapping("/status/{orderId}")
    @Operation(summary = "查询支付状态")
    public Result queryPayStatus(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            // 重新查询微信支付状态
            Integer status = paymentService.queryPayStatus(payment.getOutTradeNo());
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("status", status);
            result.put("statusText", getStatusText(status));
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询支付状态失败: " + e.getMessage());
        }
    }

    /**
     * 关闭支付订单
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/close/{orderId}")
    @Operation(summary = "关闭支付订单")
    public Result closePayOrder(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            boolean closed = paymentService.closePayOrder(payment.getOutTradeNo());
            if (closed) {
                return Result.success("关闭支付订单成功");
            } else {
                return Result.error("关闭支付订单失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("关闭支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付回调接口
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
            e.printStackTrace();
        }
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