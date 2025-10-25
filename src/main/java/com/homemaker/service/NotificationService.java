package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Notification;

import java.util.List;

/**
 * 通知Service接口
 */
public interface NotificationService extends IService<Notification> {
    
    /**
     * 创建通知
     * @param notification 通知信息
     * @return 是否创建成功
     */
    boolean createNotification(Notification notification);
    
    /**
     * 查询用户的通知列表
     * @param userId 用户ID
     * @param isRead 是否已读
     * @return 通知列表
     */
    List<Notification> findUserNotifications(Long userId, Integer isRead);
    
    /**
     * 查询护工的通知列表
     * @param nurseId 护工ID
     * @param isRead 是否已读
     * @return 通知列表
     */
    List<Notification> findNurseNotifications(Long nurseId, Integer isRead);
    
    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 是否标记成功
     */
    boolean markAsRead(Long id);
    
}