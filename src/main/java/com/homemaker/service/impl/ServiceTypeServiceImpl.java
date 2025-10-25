package com.homemaker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.homemaker.entity.ServiceType;
import com.homemaker.mapper.ServiceTypeMapper;
import com.homemaker.service.ServiceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务类型Service实现类
 */
@Service
public class ServiceTypeServiceImpl extends ServiceImpl<ServiceTypeMapper, ServiceType> implements ServiceTypeService {
    
    @Autowired
    private ServiceTypeMapper serviceTypeMapper;
    
    @Override
    public List<ServiceType> findAllServiceTypes() {
        return serviceTypeMapper.selectList(null);
    }
    
    @Override
    public ServiceType findServiceTypeById(Long id) {
        return serviceTypeMapper.selectById(id);
    }
    
}