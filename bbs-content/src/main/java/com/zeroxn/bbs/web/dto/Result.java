package com.zeroxn.bbs.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @Author: lisang
 * @DateTime: 2023-10-10 21:11:58
 * @Description: 通用返回数据类
 */
@Schema(description = "通用返回数据类")
@Data
public class Result<T> {
    /**
     * 返回响应码
     */
    @Schema(description = "返回的响应码，200:正常 400:请求参数错误 401:没有权限 500:服务端错误")
    private Integer code;
    /**
     * 返回消息
     */
    @Schema(description = "携带的返回消息")
    private String message;
    /**
     * 时间戳
     */
    @Schema(description = "当前时间戳")
    private Long timestamp;
    /**
     * 需要返回的数据
     */
    @Schema(description = "返回数据")
    private T data;

    /**
     * 请求失败的响应方法
     * @param status http状态码
     * @param message 错误消息
     * @return 返回数据类
     */
    public static Result<String> failed(HttpStatus status, String message) {
        return new Result<>(status, message);
    }

    /**
     * 重载方法
     * @param message 错误消息
     * @return 返回http错误码400的数据类
     */
    public static Result<String> failed(String message) {
        return new Result<>(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 创建请求成功携带返回数据和消息的返回数据类
     * @param message 返回消息
     * @param data 返回数据
     * @return 返回数据类
     * @param <T> 泛型
     */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(HttpStatus.OK, message, data);
    }

    /**
     * 重载方法 只返回数据
     * @param data 返回数据
     * @return 返回数据类
     * @param <T> 泛型
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(HttpStatus.OK, "ok", data);
    }

    public static Result<String> ok(String message) {
        return new Result<>(HttpStatus.OK, message);
    }

    public static Result<String> ok() {
        return new Result<>(HttpStatus.OK, "ok");
    }

    public Result() {}

    public Result(HttpStatus code, String message) {
        this.code = code.value();
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(HttpStatus code, String message, T data) {
        this.code = code.value();
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}
