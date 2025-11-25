package com.homemaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用Spring的定时任务功能
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 此类仅用于启用定时任务功能
    // 具体的定时任务实现将在其他服务类中定义
}