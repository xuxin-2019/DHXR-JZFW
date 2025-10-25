package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Evaluation;

/**
 * 评价Service接口
 */
public interface EvaluationService extends IService<Evaluation> {
    
    /**
     * 创建评价
     * @param evaluation 评价信息
     * @return 是否创建成功
     */
    boolean createEvaluation(Evaluation evaluation);
    
    /**
     * 根据订单ID查询评价
     * @param orderId 订单ID
     * @return 评价信息
     */
    Evaluation findByOrderId(Long orderId);
    
}