package com.homemaker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.entity.Refund;
import com.homemaker.entity.RefundVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 退款申请Mapper接口
 */
@Mapper
public interface RefundMapper extends BaseMapper<Refund> {

    /**
     * 根据订单ID查询退款记录
     * @param orderId 订单ID
     * @return 退款记录
     */
    Refund selectByOrderId(Long orderId);

    /**
     * 根据退款单号查询退款记录
     * @param refundNo 退款单号
     * @return 退款记录
     */
    Refund selectByRefundNo(String refundNo);

    /**
     * 根据商户退款单号查询退款记录
     * @param outRefundNo 商户退款单号
     * @return 退款记录
     */
    Refund selectByOutRefundNo(String outRefundNo);

    Page<RefundVo> selectByPage(Page<RefundVo> page, Map<String,Object> params);
}