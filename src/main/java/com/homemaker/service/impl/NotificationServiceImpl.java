package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Notification;
import com.homemaker.mapper.NotificationMapper;
import com.homemaker.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 通知Service实现类
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Override
    public boolean createNotification(Notification notification) {
        // 设置默认状态为未读
        notification.setIsRead(0);
        
        // 设置创建时间
        notification.setCreateTime(new Date());
        
        // 保存通知
        return save(notification);
    }
    
    @Override
    public List<Notification> findUserNotifications(Long userId, Integer isRead) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return notificationMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Notification> findNurseNotifications(Long nurseId, Integer isRead) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("nurse_id", nurseId);
        
        if (isRead != null) {
            queryWrapper.eq("is_read", isRead);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return notificationMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean markAsRead(Long id) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setIsRead(1);
        
        return updateById(notification);
    }
    
}