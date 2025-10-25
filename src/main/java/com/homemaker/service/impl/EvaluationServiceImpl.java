package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Evaluation;
import com.homemaker.mapper.EvaluationMapper;
import com.homemaker.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 评价Service实现类
 */
@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {
    
    @Autowired
    private EvaluationMapper evaluationMapper;
    
    @Override
    public boolean createEvaluation(Evaluation evaluation) {
        // 设置创建时间
        evaluation.setCreateTime(new Date());
        
        // 保存评价
        return save(evaluation);
    }
    
    @Override
    public Evaluation findByOrderId(Long orderId) {
        return evaluationMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Evaluation>()
                        .eq("order_id", orderId)
        );
    }
    
}