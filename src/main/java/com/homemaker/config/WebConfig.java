package com.homemaker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于注册拦截器和配置拦截路径
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器
        registry.addInterceptor(loginInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns(
                        // 根路径
                        "/",
                        "/homemaker",
                        "/homemaker/",
                        // 登录相关接口
                        "/api/wx/login",
                        "/api/admin/login",
                        "/api/wx/validate",
                        // 测试接口
                        "/api/test/**",
                        // 支付相关接口
                        "/homemaker/api/pay/notify",
                        "/homemaker/api/pay/refund-notify",
                        "/homemaker/api/payment/status",
                        // 静态资源
                        "/static/**",
                        // Swagger文档相关
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // 图标
                        "/homemaker/favicon.ico"
                );
    }
}