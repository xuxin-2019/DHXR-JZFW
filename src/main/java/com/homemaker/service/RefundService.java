package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.homemaker.entity.Refund;
import com.homemaker.entity.RefundVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 退款Service接口
 */
public interface RefundService extends IService<Refund> {

    /**
     * 创建退款申请
     * @param orderId 订单ID
     * @param reason 退款原因
     * @param userId 用户ID
     * @return 退款记录
     */
    Refund createRefund(Long orderId, String reason, Long userId);

    /**
     * 提交微信退款申请
     * @param refund 退款记录
     * @return 退款结果
     */
    Map<String, Object> submitWxRefund(Refund refund);

    /**
     * 审核退款申请
     * @param refundId 退款ID
     * @param status 审核状态 2:通过 3:拒绝
     * @param remark 审核备注
     * @param adminId 管理员ID
     * @return 是否审核成功
     */
    boolean auditRefund(Long refundId, Integer status, String remark, Long adminId);

    /**
     * 根据订单ID查询退款记录
     * @param orderId 订单ID
     * @return 退款记录
     */
    Refund getRefundByOrderId(Long orderId);

    /**
     * 根据退款单号查询退款记录
     * @param refundNo 退款单号
     * @return 退款记录
     */
    Refund getRefundByRefundNo(String refundNo);

    /**
     * 根据商户退款单号查询退款记录
     * @param outRefundNo 商户退款单号
     * @return 退款记录
     */
    Refund getRefundByOutRefundNo(String outRefundNo);

    /**
     * 更新退款状态
     * @param refund 退款记录
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateRefundStatus(Refund refund, Integer status);

    /**
     * 处理退款回调
     * @param notifyData 回调数据
     * @return 处理结果
     */
    boolean handleRefundNotify(String notifyData);

    /**
     * 查询退款状态
     * @param outRefundNo 商户退款单号
     * @return 退款状态
     */
    Integer queryRefundStatus(String outRefundNo);

    /**
     * 分页查询退款记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<RefundVo> pageRefund(Integer pageNum, Integer pageSize, Map<String, Object> params);
}