package com.homemaker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homemaker.common.Result;
import com.homemaker.service.WxLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 登录拦截器
 * 用于拦截用户（1）和护工（2）的请求，验证token有效性
 * 管理员（0）不需要拦截
 */
@Component
@ConfigurationProperties(prefix = "login.interceptor")
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    // 从配置文件读取跳过拦截的路径
    private java.util.List<String> rootPaths = new java.util.ArrayList<>();
    private java.util.List<String> loginPaths = new java.util.ArrayList<>();
    private java.util.List<String> staticPrefixes = new java.util.ArrayList<>();
    private java.util.List<String> staticExtensions = new java.util.ArrayList<>();
    private java.util.List<String> specificStaticFiles = new java.util.ArrayList<>();

    @Autowired
    private WxLoginService wxLoginService;

    @Autowired
    private ObjectMapper objectMapper;
    
    // getter和setter方法
    public java.util.List<String> getRootPaths() {
        return rootPaths;
    }
    
    public void setRootPaths(java.util.List<String> rootPaths) {
        this.rootPaths = rootPaths;
    }
    
    public java.util.List<String> getLoginPaths() {
        return loginPaths;
    }
    
    public void setLoginPaths(java.util.List<String> loginPaths) {
        this.loginPaths = loginPaths;
    }
    
    public java.util.List<String> getStaticPrefixes() {
        return staticPrefixes;
    }
    
    public void setStaticPrefixes(java.util.List<String> staticPrefixes) {
        this.staticPrefixes = staticPrefixes;
    }
    
    public java.util.List<String> getStaticExtensions() {
        return staticExtensions;
    }
    
    public void setStaticExtensions(java.util.List<String> staticExtensions) {
        this.staticExtensions = staticExtensions;
    }
    
    public java.util.List<String> getSpecificStaticFiles() {
        return specificStaticFiles;
    }
    
    public void setSpecificStaticFiles(java.util.List<String> specificStaticFiles) {
        this.specificStaticFiles = specificStaticFiles;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestUri = request.getRequestURI();
        logger.info("拦截器接收到请求: URI={}, ContextPath={}", requestUri, request.getContextPath());
        logger.info("请求完整URL: {}", request.getRequestURL());
        
        // 跳过拦截的路径
        if (isSkipIntercept(requestUri)) {
            return true;
        }
        
        // 检查是否有管理员session，如果有则直接通过（管理员不需要拦截）
        if (request.getSession().getAttribute("userType") != null && (Integer)request.getSession().getAttribute("userType") == 0) {
            return true;
        }
        
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        
        // 如果token以Bearer开头，去除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证token
        if (token == null || token.isEmpty()) {
            // token为空，返回未登录状态
            Result result = Result.error(401, "未登录，请先登录");
            responseToJson(response, result);
            return false;
        }
        
        // 调用服务层验证token
        Map<String, Object> userInfo = wxLoginService.validateToken(token);
        
        if (userInfo == null) {
            // token无效或已过期
            Result result = Result.error(401, "登录已过期，请重新登录");
            responseToJson(response, result);
            return false;
        }
        
        // token有效，将用户信息存入request属性中，供后续使用
        request.setAttribute("userId", userInfo.get("id"));
        request.setAttribute("userRole", userInfo.get("role"));
        
        return true;
    }
    
    /**
     * 判断是否需要跳过拦截
     * @param requestUri 请求路径
     * @return 是否跳过拦截
     */
    private boolean isSkipIntercept(String requestUri) {
        logger.info("检查路径是否跳过拦截: {}", requestUri);
        
        // 标准化请求路径（去除可能的尾部斜杠）
        String normalizedUri = requestUri;
        if (normalizedUri.endsWith("/") && normalizedUri.length() > 1) {
            normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
        }
        
        logger.info("标准化后的路径: {}", normalizedUri);
        
        // 检查根路径和上下文路径
        if (rootPaths.contains(requestUri) || rootPaths.contains(normalizedUri)) {
            logger.info("跳过拦截：根路径、上下文路径或首页");
            return true;
        }
        
        // 检查登录相关接口
        for (String loginPath : loginPaths) {
            if (requestUri.contains(loginPath)) {
                logger.info("跳过拦截：登录相关接口");
                return true;
            }
        }
        
        // 检查特定静态资源文件
        if (specificStaticFiles.contains(requestUri)) {
            logger.info("跳过拦截：特定静态资源文件");
            return true;
        }
        
        // 检查静态资源路径前缀
        for (String prefix : staticPrefixes) {
            if (requestUri.startsWith(prefix)) {
                logger.info("跳过拦截：静态资源");
                return true;
            }
        }
        
        // 检查静态资源文件扩展名
        for (String extension : staticExtensions) {
            if (requestUri.endsWith(extension)) {
                logger.info("跳过拦截：静态资源");
                return true;
            }
        }
        
        logger.info("不跳过拦截：需要验证token");
        return false;
    }
    
    /**
     * 将结果对象转换为JSON并写入响应
     * @param response 响应对象
     * @param result 结果对象
     * @throws Exception 异常
     */
    private void responseToJson(HttpServletResponse response, Result result) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
}