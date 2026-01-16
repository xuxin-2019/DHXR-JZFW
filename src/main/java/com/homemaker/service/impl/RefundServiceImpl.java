package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.*;
import com.homemaker.mapper.RefundMapper;
import com.homemaker.service.NotificationService;
import com.homemaker.service.OrderService;
import com.homemaker.service.PaymentService;
import com.homemaker.service.RefundService;
import com.homemaker.util.WechatPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 退款Service实现类
 */
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundServiceImpl.class);

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WechatPayUtil wechatPayUtil;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public Refund createRefund(Long orderId, String reason, Long userId) {
        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态
        if (order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3 && order.getStatus() != 6) {
            throw new RuntimeException("订单状态不允许退款");
        }

        // 查询支付记录
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null || payment.getStatus() != 3) {
            throw new RuntimeException("订单未支付或支付未成功");
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
        refund.setRefundAmount(payment.getAmount());//暂时只支持全额退款
        refund.setTotalAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus(1); // 申请中
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());

        save(refund);
        
        // 发送退款申请创建通知
        sendRefundNotification(refund, order, "您的退款申请已提交，我们将尽快为您处理");
        
        return refund;
    }

    @Override
    public Map<String, Object> submitWxRefund(Refund refund) {
        try {
            logger.info("开始提交微信退款申请，退款ID：{}", refund.getId());
            
            // 查询支付记录获取相关信息
            Payment payment = paymentService.getById(refund.getPaymentId());
            if (payment == null) {
                logger.error("支付记录不存在，支付ID：{}", refund.getPaymentId());
                throw new RuntimeException("支付记录不存在");
            }
            
            logger.info("获取到支付记录，订单号：{}，交易号：{}", payment.getOutTradeNo(), payment.getTransactionId());
            
            // 将金额转换为分（微信支付API使用分作为货币单位）
            Integer totalFee = payment.getAmount().multiply(new BigDecimal(100)).intValue();
            Integer refundFee = refund.getRefundAmount().multiply(new BigDecimal(100)).intValue();
            
            logger.info("退款金额信息 - 总金额：{}分，退款金额：{}分", totalFee, refundFee);
            
            // 调用微信支付退款API
            Map<String, String> refundResult = wechatPayUtil.refund(
                    refund.getTransactionId(), // 微信支付交易号
                    payment.getOutTradeNo(),   // 商户订单号
                    refund.getOutRefundNo(),   // 商户退款单号
                    totalFee,                  // 订单总金额（分）
                    refundFee,                 // 退款金额（分）
                    refund.getReason()         // 退款原因
            );
            
            if (refundResult == null) {
                logger.error("调用微信退款API返回结果为空，退款ID：{}", refund.getId());
                throw new RuntimeException("调用微信退款API失败：返回结果为空");
            }
            
            logger.info("微信退款API返回结果：{}", refundResult);
            
            // 检查退款结果
            if (!"SUCCESS".equals(refundResult.get("result_code"))) {
                String errorCode = refundResult.get("err_code") != null ? refundResult.get("err_code") : "未知错误码";
                String errorMsg = refundResult.get("err_code_des") != null ? refundResult.get("err_code_des") : "未知错误";
                logger.error("微信退款失败（result_code）：错误码={}，错误信息={}，退款ID：{}", errorCode, errorMsg, refund.getId());
                updateRefundStatus(refund,6);//更新退款状态为退款失败
                throw new RuntimeException("微信退款失败：" + errorMsg);
            }
            
            logger.info("微信退款申请提交成功，退款ID：{}，微信退款单号：{}", 
                    refund.getId(), refundResult.get("refund_id"));
            
            // 返回退款结果
            Map<String, Object> result = new HashMap<>();
            result.put("refundId", refund.getId());
            result.put("refundNo", refund.getRefundNo());
            result.put("outRefundNo", refund.getOutRefundNo());
            result.put("wechatRefundId", refundResult.get("refund_id"));
            result.put("status", refund.getStatus());
            
            return result;
        } catch (Exception e) {
            logger.error("提交微信退款申请失败，退款ID：{}", refund.getId(), e);
            throw new RuntimeException("提交微信退款申请失败：" + e.getMessage());
        }
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
            
            // 发送退款审核通过通知
            Order order = orderService.getById(refund.getOrderId());
            sendRefundNotification(refund, order, "您的退款申请已通过审核，正在为您办理退款");
            
            // 提交微信退款
            submitWxRefund(refund);
        } else if (updated && status == 3) {
            // 发送退款审核拒绝通知
            Order order = orderService.getById(refund.getOrderId());
            sendRefundNotification(refund, order, "您的退款申请未通过审核，原因：" + remark);
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
                order.setStatus(6); // 已取消
                orderService.updateById(order);
            }
        }
        return updateById(refund);
    }

    @Override
    public boolean handleRefundNotify(String notifyData) {
        try {
            logger.info("收到微信退款回调通知: {}", notifyData);
            
            // 将XML格式的回调数据转换为Map
            Map<String, String> notifyMap = wechatPayUtil.xmlToMap(notifyData);
            
            // 验证回调签名
//            boolean signatureValid = wechatPayUtil.verifySign(notifyMap);
//            if (!signatureValid) {
//                logger.error("微信退款回调签名验证失败");
//                return false;
//            }

            // 检查返回状态
            if (!"SUCCESS".equals(notifyMap.get("return_code"))) {
                logger.error("微信退款回调返回失败: {}", notifyMap.get("return_msg"));
                return false;
            }
            
            // 解密退款通知的加密信息
            String reqInfo = notifyMap.get("req_info");
            if (reqInfo == null || reqInfo.isEmpty()) {
                logger.error("微信退款回调缺少加密信息req_info");
                return false;
            }
            
            // 解密req_info字段
            String decryptedReqInfo = wechatPayUtil.decryptRefundNotify(reqInfo);
            if (decryptedReqInfo == null) {
                logger.error("微信退款回调加密信息解密失败");
                return false;
            }
            
            logger.info("解密后的微信退款回调信息: {}", decryptedReqInfo);
            
            // 将解密后的XML转换为Map
            Map<String, String> refundNotifyMap = wechatPayUtil.xmlToMap(decryptedReqInfo);
            
            // 获取退款相关信息
            String outRefundNo = refundNotifyMap.get("out_refund_no");
            String refundStatus = refundNotifyMap.get("refund_status");
            
            // 查询退款记录
            Refund refund = getRefundByOutRefundNo(outRefundNo);
            if (refund == null) {
                logger.error("未找到对应的退款记录: {}", outRefundNo);
                return false;
            }
            
            // 检查退款状态，避免重复处理
            if (refund.getStatus() == 5 || refund.getStatus() == 6) {
                logger.warn("退款记录已处理过: {}", outRefundNo);
                return true; // 返回true避免微信重复通知
            }
            
            // 更新退款状态
            boolean updated = false;
            if ("SUCCESS".equals(refundStatus)) {
                // 退款成功
                updated = updateRefundStatus(refund, 5);
                logger.info("退款成功，退款ID: {}, 订单ID: {}", refund.getId(), refund.getOrderId());
                
                // 发送退款成功通知
                Order order = orderService.getById(refund.getOrderId());
                sendRefundNotification(refund, order, "您的退款已成功到账，退款金额：" + refund.getRefundAmount() + "元");
            } else if ("FAIL".equals(refundStatus)) {
                // 退款失败
                refund.setRemark(notifyMap.get("err_code_des"));
                updated = updateRefundStatus(refund, 6);
                logger.error("退款失败，退款ID: {}, 原因: {}", 
                        refund.getId(), notifyMap.get("err_code_des"));
                
                // 发送退款失败通知
                Order order = orderService.getById(refund.getOrderId());
                sendRefundNotification(refund, order, "退款失败，原因：" + notifyMap.get("err_code_des") + "，请联系客服处理");
            } else if ("CHANGE".equals(refundStatus)) {
                // 退款异常，需要人工介入处理
                refund.setRemark("退款状态异常: " + refundStatus);
                updated = updateRefundStatus(refund, 6);
                logger.error("退款状态异常，退款ID: {}, 状态: {}", 
                        refund.getId(), refundStatus);
                
                // 发送退款异常通知
                Order order = orderService.getById(refund.getOrderId());
                sendRefundNotification(refund, order, "退款处理异常，请联系客服处理");
            }
            
            if (!updated) {
                logger.error("更新退款状态失败: {}", outRefundNo);
                return false;
            }
            
            logger.info("微信退款回调处理成功，退款ID: {}, 状态: {}", 
                    refund.getId(), refundStatus);
            return true;
        } catch (Exception e) {
            logger.error("处理微信退款回调异常", e);
            return false;
        }
    }

    @Override
    public Integer queryRefundStatus(String outRefundNo) {
        // 这里将在WechatPayUtil实现后调用相应方法
        // 暂时返回数据库中的状态
        Refund refund = getRefundByOutRefundNo(outRefundNo);
        return refund != null ? refund.getStatus() : null;
    }

    @Override
    public IPage<RefundVo> pageRefund(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<RefundVo> page = new Page<>(pageNum, pageSize);
        // 这里可以根据需要实现复杂的分页查询
        // 暂时简单实现

        Page<RefundVo> refundVoPage = refundMapper.selectByPage(page,params);

        return refundVoPage;
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
    
    /**
     * 发送退款通知
     * @param refund 退款记录
     * @param order 订单信息
     * @param content 通知内容
     */
    private void sendRefundNotification(Refund refund, Order order, String content) {
        try {
            Notification notification = new Notification();
            notification.setUserId(order.getUserId());
            notification.setTitle("退款通知");
            notification.setContent(content);
            notification.setType(1); // 订单通知类型
            
            notificationService.createNotification(notification);
            logger.info("发送退款通知成功，用户ID: {}, 退款ID: {}", order.getUserId(), refund.getId());
        } catch (Exception e) {
            logger.error("发送退款通知失败，用户ID: {}, 退款ID: {}", 
                    order.getUserId(), refund.getId(), e);
        }
    }
}