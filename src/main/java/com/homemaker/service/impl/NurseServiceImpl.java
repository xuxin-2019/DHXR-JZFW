package com.homemaker.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.NurseVO;
import com.homemaker.mapper.NurseMapper;
import com.homemaker.service.NurseService;
import com.homemaker.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 护工Service实现类
 */
@Service
public class NurseServiceImpl extends ServiceImpl<NurseMapper, Nurse> implements NurseService {
    
    @Autowired
    private NurseMapper nurseMapper;
    
    @Autowired
    private OrderService orderService;
    
    @Override
    public Nurse findByPhone(String phone) {
        return nurseMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Nurse>().eq("phone", phone));
    }
    
    @Override
    public Nurse findByOpenid(String openid) {
        return nurseMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Nurse>().eq("openid", openid));
    }
    
    @Override
    public boolean register(Nurse nurse) {
        // 检查护工是否已存在
        Nurse existingNurse = findByPhone(nurse.getPhone());
        if (existingNurse != null) {
            return false;
        }
        
        // 设置默认状态和评分
        nurse.setStatus(1); // 默认空闲
        nurse.setRating(new BigDecimal(5.0)); // 默认5星
        nurse.setServiceCount(0);
        
        // 设置创建时间
        nurse.setCreateTime(new Date());
        nurse.setUpdateTime(new Date());
        
        // 保存护工信息
        return save(nurse);
    }
    
    @Override
    public Nurse login(String phone, String password) {
        // 由于移除了密码字段，仅通过手机号查询护工
        // 注意：此方法仅保留接口兼容性，实际登录应使用微信登录
        return findByPhone(phone);
    }
    
    @Override
    public List<Nurse> findFreeNursesByServiceType(Long serviceTypeId) {
        return nurseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Nurse>()
                        .eq("service_type_id", serviceTypeId)
                        .eq("status", 1) // 1表示空闲
        );
    }
    
    @Override
    public List<Map<String, Object>> findFreeNursesByServiceTypeAndTimeRange(Long serviceTypeId, String startTime, String endTime) {
        // 查询指定服务类型的所有护工
        List<Nurse> allNurses = nurseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Nurse>()
                        .eq("service_type_id", serviceTypeId)
        );
        
        // 为每个护工添加isFree属性
        List<Map<String, Object>> nurseListWithAvailability = new ArrayList<>();
        for (Nurse nurse : allNurses) {
            Map<String, Object> nurseMap = new HashMap<>();
            // 添加护工的基本信息
            nurseMap.put("id", nurse.getId());
            nurseMap.put("name", nurse.getName());
            nurseMap.put("phone", nurse.getPhone());
            nurseMap.put("age", nurse.getAge());
            nurseMap.put("serviceTypeId", nurse.getServiceTypeId());
            nurseMap.put("status", nurse.getStatus());
            nurseMap.put("rating", nurse.getRating());
            nurseMap.put("serviceCount", nurse.getServiceCount());
            nurseMap.put("createTime", nurse.getCreateTime());
            
            // 判断护工是否空闲（在指定时间范围内没有已接单订单）
            boolean isFree = true;
            if (startTime != null && endTime != null && !startTime.isEmpty() && !endTime.isEmpty()) {
                isFree = !orderService.hasNurseAcceptedOrderInTimeRange(nurse.getId(), startTime, endTime);
            }
            nurseMap.put("isFree", isFree);
            
            nurseListWithAvailability.add(nurseMap);
        }
        
        return nurseListWithAvailability;
    }
    
    @Override
    public boolean updateNurseStatus(Long id, Integer status) {
        Nurse nurse = new Nurse();
        nurse.setId(id);
        nurse.setStatus(status);
        nurse.setUpdateTime(new Date());
        return updateById(nurse);
    }
    
    @Override
    public boolean updateNurseRating(Long id, Double rating) {
        Nurse nurse = getById(id);
        if (nurse != null) {
            // 更新评分和服务次数
            BigDecimal currentRating = nurse.getRating();
            Integer currentCount = nurse.getServiceCount();
            
            // 计算新评分 = (当前评分 * 当前服务次数 + 新评分) / (当前服务次数 + 1)
            BigDecimal newRating = currentRating.multiply(new BigDecimal(currentCount))
                    .add(new BigDecimal(rating))
                    .divide(new BigDecimal(currentCount + 1), 2, BigDecimal.ROUND_HALF_UP);
            
            nurse.setRating(newRating);
            nurse.setServiceCount(currentCount + 1);
            nurse.setUpdateTime(new Date());
            
            return updateById(nurse);
        }
        return false;
    }
    
    @Override
    public IPage<NurseVO> findNursesByPage(Page<NurseVO> page, String phone, String name) {
        // 创建查询参数Map
        Map<String, Object> params = new HashMap<>();
        
        // 添加手机号查询参数（如果不为空）
        if (phone != null && !phone.isEmpty()) {
            params.put("phone", phone);
        }
        
        // 添加姓名查询参数（如果不为空）
        if (name != null && !name.isEmpty()) {
            params.put("name", name);
        }
        
        // 执行联表分页查询
        return nurseMapper.selectNurseListWithDetails(page, params);
    }
}