package com.homemaker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homemaker.entity.Nurse;
import com.homemaker.entity.NurseVO;

import java.util.Map;

public interface NurseMapper extends BaseMapper<Nurse> {
    
    /**
     * 分页查询护工列表，包含服务类型名称
     * @param page 分页对象
     * @param params 查询参数
     * @return 护工VO分页结果
     */
    IPage<NurseVO> selectNurseListWithDetails(Page<NurseVO> page, Map<String, Object> params);
    
}