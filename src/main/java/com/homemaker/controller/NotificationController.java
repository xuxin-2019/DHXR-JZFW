package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.Notification;
import com.homemaker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知Controller
 */
@RestController
@RequestMapping("/api/notification")
@Tag(name = "通知管理", description = "通知相关的接口")
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 创建通知
     * @param notification 通知信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建通知", description = "创建新的通知")
    public Result createNotification(@RequestBody Notification notification) {
        boolean result = notificationService.createNotification(notification);
        if (result) {
            return Result.success("创建成功");
        } else {
            return Result.error("创建失败");
        }
    }

    /**
     * 查询用户通知
     * @param userId 用户ID
     * @param isRead 是否已读（可选，null表示查询所有）
     * @return 通知列表
     */
    @GetMapping("/user")
    @Operation(summary = "查询用户通知", description = "查询指定用户的通知列表，可根据已读状态筛选")
    public Result findUserNotifications(@RequestParam Long userId, @RequestParam(required = false) Integer isRead) {
        List<Notification> notifications = notificationService.findUserNotifications(userId, isRead);
        return Result.success("查询成功", notifications);
    }

    /**
     * 查询护工通知
     * @param nurseId 护工ID
     * @param isRead 是否已读（可选，null表示查询所有）
     * @return 通知列表
     */
    @GetMapping("/nurse")
    @Operation(summary = "查询护工通知", description = "查询指定护工的通知列表，可根据已读状态筛选")
    public Result findNurseNotifications(@RequestParam Long nurseId, @RequestParam(required = false) Integer isRead) {
        List<Notification> notifications = notificationService.findNurseNotifications(nurseId, isRead);
        return Result.success("查询成功", notifications);
    }

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 更新结果
     */
    @PostMapping("/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    public Result markAsRead(@RequestParam Long id) {
        boolean result = notificationService.markAsRead(id);
        if (result) {
            return Result.success("标记成功");
        } else {
            return Result.error("标记失败");
        }
    }

}