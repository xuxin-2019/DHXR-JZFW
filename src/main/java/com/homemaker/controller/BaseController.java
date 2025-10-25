package com.homemaker.controller;

import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 基础控制器类
 */
@RestController
public class BaseController {

    /**
     * 获取当前登录用户ID（从session中获取）
     * @param request 请求对象
     * @return 用户ID
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Long) session.getAttribute("userId");
    }

    /**
     * 获取当前登录护工ID（从session中获取）
     * @param request 请求对象
     * @return 护工ID
     */
    protected Long getCurrentNurseId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Long) session.getAttribute("nurseId");
    }

    /**
     * 获取当前登录管理员ID（从session中获取）
     * @param request 请求对象
     * @return 管理员ID
     */
    protected Long getCurrentAdminId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Long) session.getAttribute("adminId");
    }

    /**
     * 获取当前登录用户类型
     * @param request 请求对象
     * @return 用户类型（1：普通用户，2：护工，3：管理员）
     */
    protected Integer getCurrentUserType(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Integer) session.getAttribute("userType");
    }

}