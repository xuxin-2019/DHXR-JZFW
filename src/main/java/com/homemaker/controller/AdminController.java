package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.Admin;
import com.homemaker.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 管理员Controller
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "管理员相关接口")
public class AdminController extends BaseController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @param request 请求对象
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员账户登录系统")
    public Result login(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            // 设置session，添加管理员ID和角色标识（0表示管理员）
            HttpSession session = request.getSession();
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("userType", 0); // 0表示管理员角色
            return Result.success("登录成功", admin);
        } else {
            return Result.error("登录失败，用户名或密码错误");
        }
    }

    /**
     * 获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取管理员信息", description = "根据ID获取管理员详细信息")
    public Result getAdminInfo(@RequestParam Long id) {
        Admin admin = adminService.getById(id);
        if (admin != null) {
            return Result.success("获取成功", admin);
        } else {
            return Result.error("管理员不存在");
        }
    }

    /**
     * 更新管理员信息
     * @param admin 管理员信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新管理员信息", description = "更新管理员的个人信息")
    public Result updateAdminInfo(@RequestBody Admin admin) {
        boolean result = adminService.updateById(admin);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

}