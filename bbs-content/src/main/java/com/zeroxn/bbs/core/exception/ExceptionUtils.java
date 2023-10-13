package com.zeroxn.bbs.core.exception;

import org.springframework.http.HttpStatus;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 17:08:38
 * @Description: 异常工具类 便捷的抛出异常
 */
public final class ExceptionUtils {

    /**
     * 抛出系统内部异常 500响应码
     * @param message 错误信息
     */
    public static void throwServerException(String message) {
        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 抛出请求异常 400响应码
     * @param message 错误消息
     */
    public static void throwRequestException(String message) {
        throw new CustomException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 通过条件抛出自定义响应码和错误信息的异常
     * @param condition 判断条件
     * @param code 响应码
     * @param message 错误信息
     */
    public static void isConditionThrow(boolean condition, HttpStatus code, String message) {
        if (condition) {
            throw new CustomException(code, message);
        }
    }

    public static void isConditionThrowServer(boolean condition, String message) {
        if (condition) {
            throwServerException(message);
        }
    }

    public static void isConditionThrowRequest(boolean condition, String message) {
        if (condition) {
            throwRequestException(message);
        }
    }
}
