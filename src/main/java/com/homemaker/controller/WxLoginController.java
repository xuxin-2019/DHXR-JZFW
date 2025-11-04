package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.service.WxLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信登录控制器
 */
@RestController
@RequestMapping("/api/wx")
@Tag(name = "微信登录", description = "微信小程序登录相关接口")
public class WxLoginController {

    @Autowired
    private WxLoginService wxLoginService;

    /**
     * 微信小程序登录
     * 支持两种登录模式：
     * 1. 显式指定角色登录：用户明确选择用户(role=1)或护工(role=2)角色
     * 2. 自动识别角色登录：未指定角色时，通过openid自动查询用户表和护工表，识别用户身份
     * @param loginData 登录数据，包含code和可选的角色标识
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "微信小程序登录", description = "微信小程序用户登录，获取自定义登录态token，支持用户(role=1)和护工(role=2)，可自动识别角色")
    public Result wxLogin(@RequestBody Map<String, Object> loginData) {
        try {
            // 验证必要参数
            String code = loginData.getOrDefault("code", "").toString();
            if (code.isEmpty()) {
                return Result.error("登录失败，缺少code参数");
            }
            
            // 获取角色标识，允许为null（自动识别模式）
            Integer role = null;
            if (loginData.containsKey("role") && loginData.get("role") != null) {
                role = loginData.get("role") instanceof Integer ?
                    (Integer) loginData.get("role") : Integer.parseInt(loginData.get("role").toString());
                
                // 验证角色有效性（如果明确指定了角色）
                if (role != 1 && role != 2) {
                    return Result.error("登录失败，无效的角色标识");
                }
            }
            
            // 记录登录模式
            if (role == null) {
                System.out.println("进入自动识别角色登录模式");
            } else {
                System.out.println("进入指定角色登录模式，角色: " + role);
            }
            
            // 更新登录数据，确保role字段被正确设置
            loginData.put("role", role);
            
            // 调用登录服务
            Map<String, Object> result = wxLoginService.wxLogin(loginData);
            
            return Result.success("登录成功", result);
            
        } catch (Exception e) {
            // 区分处理自动识别模式下的用户不存在情况
            if (e.getMessage() != null && e.getMessage().contains("未找到用户信息")) {
                // 返回特殊状态码，告诉前端用户不存在，需要手动注册
                return Result.error(404, "未找到用户信息，请先完成注册");
            }
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 校验token有效性
     * @param token 登录token
     * @return 校验结果
     */
    @GetMapping("/validate")
    @Operation(summary = "校验token有效性", description = "校验微信登录token是否有效")
    public Result validateToken(@RequestParam String token) {
        try {
            Map<String, Object> userInfo = wxLoginService.validateToken(token);
            
            if (userInfo != null) {
                return Result.success("token有效", userInfo);
            } else {
                return Result.error("token无效或已过期");
            }
            
        } catch (Exception e) {
            return Result.error("校验失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * @param token 登录token
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "微信小程序用户登出，清除登录态")
    public Result logout(@RequestHeader("Authorization") String token) {
        try {
            // 去除Bearer前缀（如果有）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            boolean result = wxLoginService.logout(token);
            
            if (result) {
                return Result.success("登出成功");
            } else {
                return Result.error("登出失败");
            }
            
        } catch (Exception e) {
            return Result.error("登出失败: " + e.getMessage());
        }
    }
}