package com.homemaker.service;

import java.util.Map;

/**
 * 微信登录服务接口
 */
public interface WxLoginService {

    /**
     * 微信小程序登录
     * @param code 微信登录code
     * @param role 角色标识：1-用户，2-护工
     * @return 登录结果，包含token和用户信息
     */
    Map<String, Object> wxLogin(Map<String, Object> loginData);

    /**
     * 校验token有效性
     * @param token 登录token
     * @return 用户信息，包含id、role等，如果token无效则返回null
     */
    Map<String, Object> validateToken(String token);

    /**
     * 用户登出
     * @param token 登录token
     * @return 是否登出成功
     */
    boolean logout(String token);
}