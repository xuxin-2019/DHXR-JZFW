package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Evaluation;
import com.homemaker.entity.User;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.dto.EvaluationDTO;
import com.homemaker.mapper.EvaluationMapper;
import com.homemaker.mapper.UserMapper;
import com.homemaker.mapper.NurseMapper;
import com.homemaker.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价Service实现类
 */
@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {
    
    @Autowired
    private EvaluationMapper evaluationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NurseMapper nurseMapper;
    
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
                new QueryWrapper<Evaluation>()
                        .eq("order_id", orderId)
        );
    }
    
    @Override
    public Page<EvaluationDTO> getEvaluationListWithNames(int page, int size, String orderId) {
        // 创建分页对象
        Page<Evaluation> pageData = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        
        // 如果提供了订单ID，则按订单ID筛选
        if (orderId != null) {
            queryWrapper.like("order_id", orderId);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        Page<Evaluation> evaluationPage = evaluationMapper.selectPage(pageData, queryWrapper);
        
        // 转换为DTO列表
        Page<EvaluationDTO> resultPage = new Page<>();
        resultPage.setTotal(evaluationPage.getTotal());
        resultPage.setCurrent(evaluationPage.getCurrent());
        resultPage.setSize(evaluationPage.getSize());
        
        // 获取所有用户ID和护工ID
        List<Evaluation> evaluations = evaluationPage.getRecords();
        
        // 如果评价列表为空，直接返回空结果
        if (evaluations == null || evaluations.isEmpty()) {
            resultPage.setRecords(new java.util.ArrayList<>());
            return resultPage;
        }
        
        List<Long> userIds = evaluations.stream().map(Evaluation::getUserId).distinct().collect(Collectors.toList());
        List<Long> nurseIds = evaluations.stream().map(Evaluation::getNurseId).distinct().collect(Collectors.toList());
        
        // 查询用户信息
        Map<Long, String> userMap = userMapper.selectList(new QueryWrapper<User>().in("id", userIds))
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        
        // 查询护工信息
        Map<Long, String> nurseMap = nurseMapper.selectList(new QueryWrapper<Nurse>().in("id", nurseIds))
                .stream().collect(Collectors.toMap(Nurse::getId, Nurse::getName));
        
        // 转换为DTO
        List<EvaluationDTO> dtoList = evaluations.stream().map(evaluation -> {
            EvaluationDTO dto = new EvaluationDTO();
            dto.setId(evaluation.getId());
            dto.setOrderId(evaluation.getOrderId());
            dto.setUserId(evaluation.getUserId());
            dto.setUserName(userMap.getOrDefault(evaluation.getUserId(), "未知用户"));
            dto.setNurseId(evaluation.getNurseId());
            dto.setNurseName(nurseMap.getOrDefault(evaluation.getNurseId(), "未知护工"));
            dto.setRating(evaluation.getRating());
            dto.setContent(evaluation.getContent());
            dto.setCreateTime(evaluation.getCreateTime());
            return dto;
        }).collect(Collectors.toList());
        
        resultPage.setRecords(dtoList);
        
        return resultPage;
    }
    
}