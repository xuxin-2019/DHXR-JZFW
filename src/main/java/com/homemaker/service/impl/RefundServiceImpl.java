package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Order;
import com.homemaker.entity.Payment;
import com.homemaker.entity.Refund;
import com.homemaker.mapper.RefundMapper;
import com.homemaker.service.OrderService;
import com.homemaker.service.PaymentService;
import com.homemaker.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * 退款Service实现类
 */
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public Refund createRefund(Long orderId, BigDecimal refundAmount, String reason, Long userId) {
        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 2 && order.getStatus() != 3 && order.getStatus() != 6) {
            throw new RuntimeException("订单状态不允许退款");
        }

        // 查询支付记录
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null || payment.getStatus() != 3) {
            throw new RuntimeException("订单未支付或支付未成功");
        }

        // 检查退款金额
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("退款金额无效");
        }

        // 检查是否已有退款申请
        Refund existingRefund = refundMapper.selectByOrderId(orderId);
        if (existingRefund != null && existingRefund.getStatus() != 3 && existingRefund.getStatus() != 6) {
            throw new RuntimeException("该订单已有退款申请处理中");
        }

        // 创建退款记录
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setPaymentId(payment.getId());
        refund.setRefundNo(generateRefundNo());
        refund.setTransactionId(payment.getTransactionId());
        refund.setOutRefundNo(generateOutRefundNo());
        refund.setRefundAmount(refundAmount);
        refund.setTotalAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus(1); // 申请中
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());

        save(refund);
        return refund;
    }

    @Override
    public Map<String, Object> submitWxRefund(Refund refund) {
        // 这里将在WechatPayUtil实现后调用相应方法
        // 暂时返回空map
        return null;
    }

    @Override
    public boolean auditRefund(Long refundId, Integer status, String remark, Long adminId) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new RuntimeException("退款记录不存在");
        }

        if (refund.getStatus() != 1) {
            throw new RuntimeException("退款申请已处理");
        }

        refund.setStatus(status);
        refund.setAdminId(adminId);
        refund.setRemark(remark);
        refund.setUpdateTime(new Date());

        boolean updated = updateById(refund);

        // 如果审核通过，提交微信退款
        if (updated && status == 2) {
            refund.setStatus(4); // 退款中
            updateById(refund);
            // 这里将在WechatPayUtil实现后调用相应方法提交微信退款
        }

        return updated;
    }

    @Override
    public Refund getRefundByOrderId(Long orderId) {
        return refundMapper.selectByOrderId(orderId);
    }

    @Override
    public Refund getRefundByRefundNo(String refundNo) {
        return refundMapper.selectByRefundNo(refundNo);
    }

    @Override
    public Refund getRefundByOutRefundNo(String outRefundNo) {
        return refundMapper.selectByOutRefundNo(outRefundNo);
    }

    @Override
    public boolean updateRefundStatus(Refund refund, Integer status) {
        refund.setStatus(status);
        refund.setUpdateTime(new Date());
        if (status == 5) { // 退款成功
            // 更新订单状态为已退款
            Order order = orderService.getById(refund.getOrderId());
            if (order != null) {
                order.setStatus(7); // 已退款
                orderService.updateById(order);
            }
        }
        return updateById(refund);
    }

    @Override
    public boolean handleRefundNotify(String notifyData) {
        // 这里将在实现微信退款回调处理逻辑后完善
        return false;
    }

    @Override
    public Integer queryRefundStatus(String outRefundNo) {
        // 这里将在WechatPayUtil实现后调用相应方法
        // 暂时返回数据库中的状态
        Refund refund = getRefundByOutRefundNo(outRefundNo);
        return refund != null ? refund.getStatus() : null;
    }

    @Override
    public IPage<Refund> pageRefund(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<Refund> page = new Page<>(pageNum, pageSize);
        // 这里可以根据需要实现复杂的分页查询
        // 暂时简单实现
        return page(page);
    }

    /**
     * 生成退款单号
     * @return 退款单号
     */
    private String generateRefundNo() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "refund_" + timestamp + "_" + uuid.substring(0, 8);
    }

    /**
     * 生成商户退款单号
     * @return 商户退款单号
     */
    private String generateOutRefundNo() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "wxrefund_" + timestamp + "_" + uuid.substring(0, 8);
    }
}