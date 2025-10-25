package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Admin;

/**
 * 管理员Service接口
 */
public interface AdminService extends IService<Admin> {
    
    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员信息
     */
    Admin findByUsername(String username);
    
    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @return 管理员信息
     */
    Admin login(String username, String password);
    
}