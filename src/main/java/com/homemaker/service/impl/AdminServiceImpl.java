package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Admin;
import com.homemaker.mapper.AdminMapper;
import com.homemaker.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 管理员Service实现类
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Override
    public Admin findByUsername(String username) {
        return adminMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Admin>().eq("username", username));
    }
    
    @Override
    public Admin login(String username, String password) {
        // 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        
        // 查询管理员
        Admin admin = adminMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Admin>()
                        .eq("username", username)
                        .eq("password", encryptedPassword)
        );
        
        return admin;
    }
    
}