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
            // 解析角色参数，支持null值（自动识别模式）
            Object roleObj = loginData.get("role");
            Integer role = null;
            if (roleObj != null) {
                role = roleObj instanceof Integer ? (Integer) roleObj : Integer.parseInt(roleObj.toString());
            }
            
            // 从userInfo对象中获取用户信息
            Map<String, Object> userInfo = (Map<String, Object>) loginData.getOrDefault("userInfo", new HashMap<>());
            String name = (String) userInfo.getOrDefault("nickName", "");
            String avatarUrl = (String) userInfo.getOrDefault("avatarUrl", "");

            // 调用微信接口获取openid和session_key
            JSONObject wxResult = wxLoginUtil.jscode2session(appid, secret, code);
            
            // 检查是否有错误
            if (wxResult.containsKey("errcode")) {
                throw new RuntimeException("微信登录失败: " + wxResult.getString("errmsg"));
            }
            
            String openid = wxResult.getString("openid");
            String sessionKey = wxResult.getString("session_key");
            
            // 处理微信手机号授权
            String phone = null;
            Object encryptedDataObj = loginData.get("encryptedData");
            Object ivObj = loginData.get("iv");
            
            if (encryptedDataObj != null && ivObj != null) {
                String encryptedData = encryptedDataObj.toString();
                String iv = ivObj.toString();
                
                // 解密手机号
                try {
                    JSONObject phoneInfo = wxLoginUtil.decryptPhoneNumber(encryptedData, sessionKey, iv);
                    if (phoneInfo != null) {
                        phone = phoneInfo.getString("phoneNumber");
                    }
                } catch (Exception e) {
                    // 解密失败不影响登录，但记录日志
                    System.err.println("手机号解密失败: " + e.getMessage());
                }
            }
            
            // 如果解密失败，尝试直接从loginData获取phone
            if (phone == null) {
                Object phoneObj = loginData.get("phone");
                if (phoneObj != null) {
                    phone = phoneObj.toString();
                }
            }
            
            // 根据角色查询不同的表
            Object userObj = null;
            Long userId = null;
            
            // 1. 自动识别角色模式
            if (role == null) {
                System.out.println("自动识别角色，openid: " + openid);
                
                // 先查询用户表
                User user = userService.findByOpenid(openid);
                if (user != null) {
                    System.out.println("自动识别成功：用户角色");
                    // 识别为用户角色
                    role = 1;
                    userObj = user;
                    userId = user.getId();
                    
                    // 如果用户存在但没有手机号，更新手机号
                    if (phone != null && (user.getPhone() == null || user.getPhone().isEmpty())) {
                        user.setPhone(phone);
                        user.setUpdateTime(new Date());
                        userService.updateById(user);
                    }
                }
                
                // 如果用户不存在，查询护工表
                if (userObj == null) {
                    Nurse nurse = nurseService.findByOpenid(openid);
                    if (nurse != null) {
                        System.out.println("自动识别成功：护工角色");
                        // 识别为护工角色
                        role = 2;
                        userObj = nurse;
                        userId = nurse.getId();
                        
                        // 如果护工存在但没有手机号，更新手机号
                        if (phone != null && (nurse.getPhone() == null || nurse.getPhone().isEmpty())) {
                            nurse.setPhone(phone);
                            nurse.setUpdateTime(new Date());
                            nurseService.updateById(nurse);
                        }
                    }
                }
                
                // 如果用户和护工都不存在，抛出异常让controller处理
                if (userObj == null) {
                    throw new RuntimeException("未找到用户信息");
                }
            }
            // 2. 指定角色登录模式
            else if (role == 1) { // 用户角色
                User user = userService.findByOpenid(openid);
                
                if (user == null) {
                    // 如果用户不存在，创建新用户
                    String address = (String) loginData.get("address");
                    user = new User();
                    // 用户ID将由数据库自动生成
                    user.setName(name);
                    user.setAvatarUrl(avatarUrl);
                    user.setOpenid(openid);
                    user.setPhone(phone != null ? phone : ""); // 确保phone字段不为null
                    user.setAddress(address);
                    user.setCreateTime(new Date());
                    user.setUpdateTime(new Date());
                    userService.save(user);
                    // 保存后，确保获取到自动生成的ID
                    userId = user.getId();
                } else {
                    // 如果用户存在但没有手机号，更新手机号
                    if (user.getPhone() == null || user.getPhone().isEmpty()) {
                        user.setPhone(phone != null ? phone : "");
                        user.setUpdateTime(new Date());
                        userService.updateById(user);
                    }
                }
                
                userObj = user;
                userId = user.getId();
            } else if (role == 2) { // 护工角色
                Nurse nurse = nurseService.findByOpenid(openid);
                
                if (nurse == null) {
                    // 如果护工不存在，创建新护工
                    int age = loginData.get("age") instanceof Integer ? 
                            (Integer) loginData.get("age") : 0;
                    // 处理serviceTypeId的类型转换，支持Integer和Long类型
                    Object serviceTypeIdObj = loginData.get("serviceTypeId");
                    Long serviceTypeId = null;
                    if (serviceTypeIdObj instanceof Long) {
                        serviceTypeId = (Long) serviceTypeIdObj;
                    } else if (serviceTypeIdObj instanceof Integer) {
                        serviceTypeId = ((Integer) serviceTypeIdObj).longValue();
                    } else if (serviceTypeIdObj instanceof String) {
                        // 也支持字符串类型的输入
                        try {
                            serviceTypeId = Long.parseLong((String) serviceTypeIdObj);
                        } catch (NumberFormatException e) {
                            serviceTypeId = null;
                        }
                    }
                    nurse = new Nurse();
                    // 护工ID将由数据库自动生成
                    nurse.setPhone(phone != null ? phone : ""); // 确保phone字段不为null
                    nurse.setName(name);
                    nurse.setAge(age);
                    nurse.setAvatarUrl(avatarUrl);
                    nurse.setServiceTypeId(serviceTypeId);
                    nurse.setOpenid(openid);
                    nurse.setStatus(1); // 默认空闲状态
                    nurse.setCreateTime(new Date());
                    nurse.setUpdateTime(new Date());
                    nurseService.save(nurse);
                    // 保存后，确保获取到自动生成的ID
                    userId = nurse.getId();
                } else {
                    // 如果护工存在但没有手机号，更新手机号
                    if (nurse.getPhone() == null || nurse.getPhone().isEmpty()) {
                        nurse.setPhone(phone != null ? phone : "");
                        nurse.setUpdateTime(new Date());
                        nurseService.updateById(nurse);
                    }
                }
                
                userObj = nurse;
                userId = nurse.getId();
            } else {
                throw new RuntimeException("无效的角色标识");
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
            // 如果是用户不存在的错误，包装为RuntimeException抛出，让controller特殊处理
            if (e.getMessage() != null && e.getMessage().contains("未找到用户信息")) {
                throw new RuntimeException(e.getMessage(), e);
            }
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