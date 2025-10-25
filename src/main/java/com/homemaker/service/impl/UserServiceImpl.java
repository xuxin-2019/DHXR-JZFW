package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.User;
import com.homemaker.mapper.UserMapper;
import com.homemaker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User findByPhone(String phone) {
        return userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("phone", phone));
    }
    
    @Override
    public User findByOpenid(String openid) {
        return userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("openid", openid));
    }
    
    @Override
    public boolean register(User user) {
        // 检查用户是否已存在
        User existingUser = findByPhone(user.getPhone());
        if (existingUser != null) {
            return false;
        }
        
        // 设置创建时间
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 保存用户信息
        return save(user);
    }
    
    @Override
    public User login(String phone, String password) {
        // 由于移除了密码字段，仅通过手机号查询用户
        // 注意：此方法仅保留接口兼容性，实际登录应使用微信登录
        return findByPhone(phone);
    }
    
    @Override
    public IPage<User> findUsersByPage(Page<User> page, String phone, String name) {
        // 创建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 添加手机号模糊查询条件（如果不为空）
        if (phone != null && !phone.isEmpty()) {
            queryWrapper.like("phone", phone);
        }
        
        // 添加姓名模糊查询条件（如果不为空）
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        // 添加排序条件（按创建时间降序）
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        return userMapper.selectPage(page, queryWrapper);
    }
}