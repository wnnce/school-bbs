package com.zeroxn.bbs.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 16:50:53
 * @Description: 自定义错误，返回错误响应码和错误消息
 */
@Getter
public class CustomException extends RuntimeException{
    private final HttpStatus code;
    private final String message;

    /**
     * 创建自定义异常
     * @param code 错误响应码
     * @param message 错误信息
     */
    public CustomException(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
    }

    public CustomException(HttpStatus code) {
        this.code = code;
        this.message = "error";
    }

    public CustomException(String message) {
        this.message = message;
        this.code = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
