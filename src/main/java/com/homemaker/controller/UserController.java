package com.homemaker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.common.Result;
import com.homemaker.entity.User;
import com.homemaker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户Controller
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     * 注意：密码字段已被移除，系统使用微信登录
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账户（已移除密码字段）")
    public Result register(@RequestBody User user) {
        boolean result = userService.register(user);
        if (result) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，用户已存在");
        }
    }

    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码（已忽略）
     * @return 登录结果
     * 注意：密码字段已被移除，仅通过手机号验证，实际应使用微信登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户账户登录系统（已移除密码验证，仅保留接口兼容性）")
    public Result login(@RequestParam String phone, @RequestParam String password) {
        User user = userService.login(phone, password);
        if (user != null) {
            return Result.success("登录成功", user);
        } else {
            return Result.error("登录失败，手机号或密码错误");
        }
    }

    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "根据ID获取用户详细信息")
    public Result getUserInfo(@RequestParam Long id) {
        User user = userService.getById(id);
        if (user != null) {
            return Result.success("获取成功", user);
        } else {
            return Result.error("用户不存在");
        }
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新用户的个人信息")
    public Result updateUserInfo(@RequestBody User user) {
        boolean result = userService.updateById(user);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
    
    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param phone 手机号（可选）
     * @param name 姓名（可选）
     * @return 用户列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "分页查询所有用户信息，支持手机号和姓名模糊搜索")
    public Result getUserList(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name) {
        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        // 执行分页查询
        IPage<User> userPage = userService.findUsersByPage(page, phone, name);
        // 返回结果
        return Result.success("查询成功", userPage);
    }

}