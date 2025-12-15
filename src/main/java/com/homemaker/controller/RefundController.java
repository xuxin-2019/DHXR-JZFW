package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.Refund;
import com.homemaker.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 退款Controller
 */
@RestController
@RequestMapping("/api/refund")
@Tag(name = "退款管理")
public class RefundController {

    @Autowired
    private RefundService refundService;

    /**
     * 创建退款申请
     * @param orderId 订单ID
     * @param reason 退款原因
     * @param userId 用户ID
     * @return 退款信息
     */
    @PostMapping("/create")
    @Operation(summary = "创建退款申请")
    public Result createRefund(@RequestParam Long orderId,
                              @RequestParam String reason,
                              @RequestParam Long userId) {
        try {
            Refund refund = refundService.createRefund(orderId, reason, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("refundId", refund.getId());
            result.put("refundNo", refund.getRefundNo());
            result.put("status", refund.getStatus());
            result.put("statusText", getStatusText(refund.getStatus()));
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询退款状态
     * @param orderId 订单ID
     * @return 退款状态
     */
    @GetMapping("/status/{orderId}")
    @Operation(summary = "查询退款状态")
    public Result queryRefundStatus(@PathVariable Long orderId) {
        try {
            Refund refund = refundService.getRefundByOrderId(orderId);
            if (refund == null) {
                return Result.error("退款记录不存在");
            }

            // 重新查询微信退款状态
            Integer status = refundService.queryRefundStatus(refund.getOutRefundNo());
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("refundNo", refund.getRefundNo());
            result.put("refundAmount", refund.getRefundAmount());
            result.put("status", status);
            result.put("statusText", getStatusText(status));
            result.put("reason", refund.getReason());
            result.put("remark", refund.getRemark());
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询退款状态失败: " + e.getMessage());
        }
    }

    /**
     * 审核退款申请（管理员接口）
     * @param params 请求参数
     * @return 操作结果
     */
    @PostMapping("/audit")
    @Operation(summary = "审核退款申请")
    public Result auditRefund(@RequestBody Map<String, Object> params) {
        Long refundId = Long.valueOf(params.get("refundId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark").toString();
        Long adminId = Long.valueOf(params.get("adminId").toString());
        try {
            if (status != 2 && status != 3) {
                return Result.error("无效的审核状态");
            }

            boolean audited = refundService.auditRefund(refundId, status, remark, adminId);
            if (audited) {
                return Result.success("审核成功");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询退款记录（管理员接口）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询退款记录")
    public Result pageRefund(@RequestParam Integer pageNum,
                            @RequestParam Integer pageSize,
                            @RequestParam(required = false) Map<String, Object> params) {
        try {
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            return Result.success(refundService.pageRefund(pageNum, pageSize, params));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询退款记录失败: " + e.getMessage());
        }
    }

    /**
     * 微信退款回调接口
     * @param request 请求
     * @param response 响应
     */
    @PostMapping("/notify")
    @Operation(summary = "微信退款回调")
    public void refundNotify(HttpServletRequest request, HttpServletResponse response) {
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
            boolean handled = refundService.handleRefundNotify(notifyData.toString());

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
     * 获取退款状态文本
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "申请中";
            case 2:
                return "已通过";
            case 3:
                return "已拒绝";
            case 4:
                return "退款中";
            case 5:
                return "退款成功";
            case 6:
                return "退款失败";
            default:
                return "未知状态";
        }
    }
}