package com.homemaker.common;

import lombok.Data;

/**
 * 统一返回结果类
 */
@Data
public class Result {

    private Integer code; // 响应码，200表示成功，其他表示失败
    private String message; // 响应消息
    private Object data; // 响应数据

    /**
     * 成功返回结果
     */
    public static Result success(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回结果
     */
    public static Result success(String message, Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回结果
     */
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 自定义状态码返回结果
     */
    public static Result error(Integer code, String message) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

}