package com.homemaker.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Evaluation;
import com.homemaker.entity.dto.EvaluationDTO;

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
    
    /**
     * 分页查询评价列表，包含用户名和护工名
     * @param page 当前页码
     * @param size 每页大小
     * @param orderId 订单ID（可选，用于筛选）
     * @return 评价列表
     */
    Page<EvaluationDTO> getEvaluationListWithNames(int page, int size, String orderId);
    
}