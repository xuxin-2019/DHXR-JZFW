package com.homemaker.exception;

import com.homemaker.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有异常
     * @param e 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统错误，请稍后再试");
    }

    /**
     * 处理业务异常
     * @param e 业务异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

}