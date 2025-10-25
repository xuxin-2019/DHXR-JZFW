package com.homemaker.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.User;
import com.homemaker.service.NurseService;
import com.homemaker.service.UserService;
import com.homemaker.service.WxLoginService;
import com.homemaker.util.WxLoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信登录服务实现类
 */
@Service
public class WxLoginServiceImpl implements WxLoginService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NurseService nurseService;
    
    @Autowired
    private WxLoginUtil wxLoginUtil;
    
    // 小程序AppID
    @Value("${wechat.mini.appid}")
    private String appid;
    
    // 小程序AppSecret
    @Value("${wechat.mini.secret}")
    private String secret;
    
    // token有效期（小时）
    private static final int TOKEN_EXPIRE_HOURS = 168; // 7天
    
    @Override
    public Map<String, Object> wxLogin(Map<String, Object> loginData) {
        try {
            String code = loginData.getOrDefault("code", "").toString();
            Integer role = loginData.getOrDefault("role", 1) instanceof Integer ?
                    (Integer) loginData.get("role") : Integer.parseInt(loginData.get("role").toString());
            
            // 从userInfo对象中获取用户信息
            Map<String, Object> userInfo = (Map<String, Object>) loginData.getOrDefault("userInfo", new HashMap<>());
            String name = (String) userInfo.getOrDefault("nickName", "");
            String avatarUrl = (String) userInfo.getOrDefault("avatarUrl", "");

            // 调用微信接口获取openid
            JSONObject wxResult = wxLoginUtil.jscode2session(appid, secret, code);
            
            // 检查是否有错误
            if (wxResult.containsKey("errcode")) {
                throw new RuntimeException("微信登录失败: " + wxResult.getString("errmsg"));
            }
            
            String openid = wxResult.getString("openid");
            
            // 根据角色查询不同的表
            Object userObj = null;
            Long userId = null;
            
            if (role == 1) { // 用户角色
                User user = userService.findByOpenid(openid);
                
                if (user == null) {
                    // 如果用户不存在，创建新用户
                    String phone = (String) loginData.get("phone");
                    String address = (String) loginData.get("address");
                    user = new User();
                    user.setName(name);
                    user.setAvatarUrl(avatarUrl);
                    user.setOpenid(openid);
                    user.setPhone(phone);
                    user.setAddress(address);
                    user.setCreateTime(new Date());
                    user.setUpdateTime(new Date());
                    userService.save(user);
                }
                
                userObj = user;
                userId = user.getId();
            } else if (role == 2) { // 护工角色
                Nurse nurse = nurseService.findByOpenid(openid);
                
                if (nurse == null) {
                    // 如果护工不存在，创建新护工
                    String phone = (String) loginData.get("phone");
                    int age = (int) loginData.get("age");
                    // 处理serviceTypeId的类型转换，支持Integer和Long类型
                    Object serviceTypeIdObj = loginData.get("serviceTypeId");
                    Long serviceTypeId = null;
                    if (serviceTypeIdObj instanceof Long) {
                        serviceTypeId = (Long) serviceTypeIdObj;
                    } else if (serviceTypeIdObj instanceof Integer) {
                        serviceTypeId = ((Integer) serviceTypeIdObj).longValue();
                    } else if (serviceTypeIdObj instanceof String) {
                        // 也支持字符串类型的输入
                        serviceTypeId = Long.parseLong((String) serviceTypeIdObj);
                    }
                    nurse = new Nurse();
                    nurse.setPhone(phone);
                    nurse.setName(name);
                    nurse.setAge(age);
                    nurse.setAvatarUrl(avatarUrl);
                    nurse.setServiceTypeId(serviceTypeId);
                    nurse.setOpenid(openid);
                    nurse.setStatus(1); // 默认空闲状态
                    nurse.setCreateTime(new Date());
                    nurse.setUpdateTime(new Date());
                    nurseService.save(nurse);
                }
                
                userObj = nurse;
                userId = nurse.getId();
            }
            
            // 生成token
            String token = wxLoginUtil.generateToken();
            
            // 创建用户信息对象用于存储在Redis中
            Map<String, Object> redisUserInfo = new HashMap<>();
            redisUserInfo.put("id", userId);
            redisUserInfo.put("role", role);
            
            // 存储token到Redis，设置过期时间
            redisTemplate.opsForValue().set("wx:token:" + token, redisUserInfo, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("id", userId);
            result.put("userInfo", userObj);
            result.put("role", role);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> validateToken(String token) {
        try {
            String key = "wx:token:" + token;
            
            // 检查token是否存在
            if (redisTemplate.hasKey(key)) {
                // 获取用户信息
                Map<String, Object> userInfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);
                
                // 重置token过期时间
                redisTemplate.expire(key, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
                return userInfo;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean logout(String token) {
        try {
            // 从Redis删除token
            String key = "wx:token:" + token;
            return redisTemplate.delete(key);
        } catch (Exception e) {
            return false;
        }
    }
}