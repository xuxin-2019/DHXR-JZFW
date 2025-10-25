package com.homemaker.exception;

import lombok.Data;

/**
 * 业务异常类
 */
@Data
public class BusinessException extends RuntimeException {

    private Integer code; // 异常状态码

    /**
     * 构造方法
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造方法
     * @param code 异常状态码
     * @param message 异常信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}