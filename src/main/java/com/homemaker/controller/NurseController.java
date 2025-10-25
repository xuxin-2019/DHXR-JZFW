package com.homemaker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.common.Result;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.NurseVO;
import com.homemaker.service.NurseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 护工Controller
 */
@RestController
@RequestMapping("/api/nurse")
@Tag(name = "护工管理", description = "护工相关接口")
public class NurseController extends BaseController {

    @Autowired
    private NurseService nurseService;

    /**
     * 护工注册
     * @param nurse 护工信息
     * @return 注册结果
     * 注意：密码字段已被移除，系统使用微信登录
     */
    @PostMapping("/register")
    @Operation(summary = "护工注册", description = "新护工注册账户（已移除密码字段）")
    public Result register(@RequestBody Nurse nurse) {
        boolean result = nurseService.register(nurse);
        if (result) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，护工已存在");
        }
    }

    /**
     * 护工登录
     * @param phone 手机号
     * @param password 密码（已忽略）
     * @return 登录结果
     * 注意：密码字段已被移除，仅通过手机号验证，实际应使用微信登录
     */
    @PostMapping("/login")
    @Operation(summary = "护工登录", description = "护工账户登录系统（已移除密码验证，仅保留接口兼容性）")
    public Result login(@RequestParam String phone, @RequestParam String password) {
        Nurse nurse = nurseService.login(phone, password);
        if (nurse != null) {
            return Result.success("登录成功", nurse);
        } else {
            return Result.error("登录失败，手机号或密码错误");
        }
    }

    /**
     * 获取护工信息
     * @param id 护工ID
     * @return 护工信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取护工信息", description = "根据ID获取护工详细信息")
    public Result getNurseInfo(@RequestParam Long id) {
        Nurse nurse = nurseService.getById(id);
        if (nurse != null) {
            return Result.success("获取成功", nurse);
        } else {
            return Result.error("护工不存在");
        }
    }

    /**
     * 更新护工信息
     * @param nurse 护工信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新护工信息", description = "更新护工的个人信息")
    public Result updateNurseInfo(@RequestBody Nurse nurse) {
        boolean result = nurseService.updateById(nurse);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 查询空闲护工
     * @param serviceTypeId 服务类型ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 空闲护工列表
     */
    @GetMapping("/free")
    @Operation(summary = "根据服务类型和时间范围查询护工列表及其空闲状态", description = "根据服务类型和时间范围查询空闲护工")
    public Result findFreeNurses(@RequestParam Long serviceTypeId, 
                               @RequestParam(required = false) String startTime,
                               @RequestParam(required = false) String endTime) {
        // 调用服务层方法，传入服务类型ID和时间范围来查询空闲护工
        // 空闲护工定义为：在指定时间范围内没有已接单状态订单的护工
        List<Map<String, Object>> nurses = nurseService.findFreeNursesByServiceTypeAndTimeRange(serviceTypeId, startTime, endTime);
        return Result.success("查询成功", nurses);
    }
    
    /**
     * 分页查询护工列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param phone 手机号（可选）
     * @param name 姓名（可选）
     * @return 护工列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询护工列表", description = "分页查询所有护工信息，支持手机号和姓名模糊搜索，包含服务类型名称")
    public Result getNurseList(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name) {
        // 创建分页对象
        Page<NurseVO> page = new Page<>(pageNum, pageSize);
        // 执行分页查询
        IPage<NurseVO> nursePage = nurseService.findNursesByPage(page, phone, name);
        // 返回结果
        return Result.success("查询成功", nursePage);
    }

    /**
     * 更新护工状态
     * @param id 护工ID
     * @param status 状态(1:空闲, 2:忙碌, 3:离线)
     * @return 更新结果
     */
    @PostMapping("/status")
    @Operation(summary = "更新护工状态", description = "更新护工的工作状态")
    public Result updateNurseStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean result = nurseService.updateNurseStatus(id, status);
        if (result) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

}