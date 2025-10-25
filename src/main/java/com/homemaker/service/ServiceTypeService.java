package com.homemaker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.homemaker.entity.ServiceType;

import java.util.List;

/**
 * 服务类型Service接口
 */
public interface ServiceTypeService extends IService<ServiceType> {
    
    /**
     * 查询所有服务类型
     * @return 服务类型列表
     */
    List<ServiceType> findAllServiceTypes();
    
    /**
     * 根据ID查询服务类型
     * @param id 服务类型ID
     * @return 服务类型信息
     */
    ServiceType findServiceTypeById(Long id);
    
}