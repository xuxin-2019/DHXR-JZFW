package com.homemaker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(String phone);
    
    /**
     * 根据微信openid查询用户
     * @param openid 微信openid
     * @return 用户信息
     */
    User findByOpenid(String openid);
    
    /**
     * 用户注册
     * @param user 用户信息
     * @return 是否注册成功
     */
    boolean register(User user);
    
    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 用户信息
     */
    User login(String phone, String password);
    
    /**
     * 分页查询用户列表
     * @param page 分页对象
     * @param phone 手机号（可选）
     * @param name 姓名（可选）
     * @return 分页结果
     */
    IPage<User> findUsersByPage(Page<User> page, String phone, String name);
}