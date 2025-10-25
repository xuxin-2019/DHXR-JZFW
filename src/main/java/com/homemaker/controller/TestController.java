package com.homemaker.controller;

import com.homemaker.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器，用于验证项目是否正常运行
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 测试接口
     * @return 测试结果
     */
    @GetMapping("/ping")
    public Result ping() {
        return Result.success("pong");
    }

    /**
     * 获取项目信息
     * @return 项目信息
     */
    @GetMapping("/info")
    public Result getInfo() {
        return Result.success("家政服务平台后端服务正常运行中");
    }

}