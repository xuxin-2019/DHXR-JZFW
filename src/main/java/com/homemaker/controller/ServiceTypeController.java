package com.homemaker.controller;

import com.homemaker.common.Result;
import com.homemaker.entity.ServiceType;
import com.homemaker.service.ServiceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务类型Controller
 */
@RestController
@RequestMapping("/api/service-type")
@Tag(name = "服务类型管理", description = "服务类型相关接口")
public class ServiceTypeController extends BaseController {

    @Autowired
    private ServiceTypeService serviceTypeService;

    /**
     * 获取所有服务类型
     * @return 服务类型列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有服务类型", description = "查询系统中所有服务类型")
    public Result getAllServiceTypes() {
        List<ServiceType> serviceTypes = serviceTypeService.findAllServiceTypes();
        return Result.success("查询成功", serviceTypes);
    }

    /**
     * 根据ID获取服务类型
     * @param id 服务类型ID
     * @return 服务类型信息
     */
    @GetMapping("/info")
    @Operation(summary = "根据ID获取服务类型", description = "根据ID查询特定服务类型信息")
    public Result getServiceTypeById(@RequestParam Long id) {
        ServiceType serviceType = serviceTypeService.findServiceTypeById(id);
        if (serviceType != null) {
            return Result.success("查询成功", serviceType);
        } else {
            return Result.error("服务类型不存在");
        }
    }

    /**
     * 添加服务类型
     * @param serviceType 服务类型信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加服务类型", description = "添加新的服务类型")
    public Result addServiceType(@RequestBody ServiceType serviceType) {
        boolean result = serviceTypeService.save(serviceType);
        if (result) {
            return Result.success("添加成功");
        } else {
            return Result.error("添加失败");
        }
    }

    /**
     * 更新服务类型
     * @param serviceType 服务类型信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新服务类型", description = "更新现有服务类型信息")
    public Result updateServiceType(@RequestBody ServiceType serviceType) {
        boolean result = serviceTypeService.updateById(serviceType);
        if (result) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 删除服务类型
     * @param id 服务类型ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "删除服务类型", description = "删除指定ID的服务类型")
    public Result deleteServiceType(@RequestParam Long id) {
        boolean result = serviceTypeService.removeById(id);
        if (result) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

}