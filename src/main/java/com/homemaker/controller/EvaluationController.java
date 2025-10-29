package com.homemaker.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.common.Result;
import com.homemaker.entity.Evaluation;
import com.homemaker.entity.dto.EvaluationDTO;
import com.homemaker.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评价Controller
 */
@RestController
@RequestMapping("/api/evaluation")
@Tag(name = "评价管理", description = "服务评价相关接口")
public class EvaluationController extends BaseController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 创建评价
     * @param evaluation 评价信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建评价", description = "对服务订单创建评价")
    public Result createEvaluation(@RequestBody Evaluation evaluation) {
        boolean result = evaluationService.createEvaluation(evaluation);
        if (result) {
            return Result.success("评价成功");
        } else {
            return Result.error("评价失败");
        }
    }

    /**
     * 根据订单ID查询评价
     * @param orderId 订单ID
     * @return 评价信息
     */
    @GetMapping("/order")
    @Operation(summary = "根据订单ID查询评价", description = "根据订单ID查询对应的评价信息")
    public Result findByOrderId(@RequestParam Long orderId) {
        Evaluation evaluation = evaluationService.findByOrderId(orderId);
        if (evaluation != null) {
            return Result.success("查询成功", evaluation);
        } else {
            return Result.error("未找到评价信息");
        }
    }

    /**
     * 分页查询评价列表
     * @param page 当前页码
     * @param size 每页大小
     * @param orderId 订单ID（可选，用于筛选）
     * @return 评价列表，包含用户名和护工名
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询评价列表", description = "分页查询所有评价，支持按订单ID筛选，并返回用户名和护工名")
    public Result list(@RequestParam(defaultValue = "1") int page, 
                       @RequestParam(defaultValue = "10") int size, 
                       @RequestParam(required = false) String orderId) {
        // 使用自定义SQL实现联表查询
        Page<EvaluationDTO> result = evaluationService.getEvaluationListWithNames(page, size, orderId);
        
        return Result.success("查询成功", result);
    }

}