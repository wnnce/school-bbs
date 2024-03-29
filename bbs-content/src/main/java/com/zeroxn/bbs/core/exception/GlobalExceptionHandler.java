package com.zeroxn.bbs.core.exception;

import com.zeroxn.bbs.web.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 16:58:09
 * @Description: 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * 处理自定义异常
     * @param ex 自定义异常
     * @return 返回错误码和错误信息
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Result<Void>> handlerCustomException(CustomException ex) {
        logger.error("抛出自定义异常，错误码：{}}，错误信息：{}", ex.getCode(), ex.getMessage());
        HttpStatus errCode = ex.getCode();
        Result<Void> result = Result.failed(errCode, ex.getMessage());
        return new ResponseEntity<>(result, errCode);
    }

    /**
     * 处理validation参数校验错误
     * @param ex 参数校验错误异常
     * @return 返回错误消息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        List<ObjectError> allErrors = ex.getAllErrors();
        String[] messages = allErrors.stream().map(ObjectError::getDefaultMessage).toArray(String[]::new);
        return Result.failed(messages[0]);
    }

    /**
     * 处理上传文件过大异常
     * @param ex MaxUploadSizeExceededException
     * @return 返回错误信息
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        logger.error("请求上传的文件过大，上传文件大小：{}", ex.getMaxUploadSize());
        return Result.failed("上传文件过大");
    }

    /**
     * 处理方法权限验证错误异常，通常情况是登录用户没有访问此接口的权限
     * @param ex AccessDeniedException
     * @return 返回403错误码和报错信息
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handlerAccessDeniedException(AccessDeniedException ex) {
        logger.error("权限验证异常，错误信息：{}", ex.getMessage());
        return Result.failed(HttpStatus.FORBIDDEN, "该用户无权限访问");
    }

    /**
     * 处理所有未被捕获的异常
     * @param ex Exception 顶级异常类
     * @return 返回500错误码和指定的错误消息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handlerException(Exception ex) {
        logger.error("未被捕获的异常，异常类型：{}，错误信息：{}", ex.getClass(), ex.getMessage());
        return Result.failed(HttpStatus.INTERNAL_SERVER_ERROR, "系统错误，请稍后再试");
    }
}
