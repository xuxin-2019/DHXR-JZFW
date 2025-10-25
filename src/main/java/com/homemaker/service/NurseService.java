package com.homemaker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.NurseVO;

import java.util.List;
import java.util.Map;

/**
 * 护工Service接口
 */
public interface NurseService extends IService<Nurse> {
    
    /**
     * 根据手机号查询护工
     * @param phone 手机号
     * @return 护工信息
     */
    Nurse findByPhone(String phone);
    
    /**
     * 根据微信openid查询护工
     * @param openid 微信openid
     * @return 护工信息
     */
    Nurse findByOpenid(String openid);
    
    /**
     * 护工注册
     * @param nurse 护工信息
     * @return 是否注册成功
     */
    boolean register(Nurse nurse);
    
    /**
     * 护工登录
     * @param phone 手机号
     * @param password 密码
     * @return 护工信息
     */
    Nurse login(String phone, String password);
    
    /**
     * 根据服务类型查询空闲护工
     * @param serviceTypeId 服务类型ID
     * @return 空闲护工列表
     */
    List<Nurse> findFreeNursesByServiceType(Long serviceTypeId);
    
    /**
     * 根据服务类型和时间范围查询护工列表及其空闲状态
     * @param serviceTypeId 服务类型ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 护工列表，每个护工对象包含isFree属性表示是否空闲
     */
    List<Map<String, Object>> findFreeNursesByServiceTypeAndTimeRange(Long serviceTypeId, String startTime, String endTime);
    
    /**
     * 更新护工状态
     * @param id 护工ID
     * @param status 状态(1:空闲, 2:忙碌, 3:离线)
     * @return 是否更新成功
     */
    boolean updateNurseStatus(Long id, Integer status);
    
    /**
     * 分页查询护工列表，包含服务类型名称
     * @param page 分页对象
     * @param phone 手机号（可选）
     * @param name 姓名（可选）
     * @return 护工VO分页结果
     */
    IPage<NurseVO> findNursesByPage(Page<NurseVO> page, String phone, String name);
    
    /**
     * 更新护工评分
     * @param id 护工ID
     * @param rating 评分
     * @return 是否更新成功
     */
    boolean updateNurseRating(Long id, Double rating);
    
}