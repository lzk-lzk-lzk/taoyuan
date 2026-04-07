package com.example.peach.common.exception;

import com.example.peach.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
// 全局异常处理
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    // 处理业务异常
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 处理请求体参数校验异常
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    // 处理表单参数绑定异常
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    // 处理普通参数校验异常
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    // 处理认证异常
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        return Result.fail(401, "账号或密码错误");
    }

    @ExceptionHandler(Exception.class)
    // 处理其它未捕获异常
    public Result<Void> handleException(Exception e) {
        return Result.fail("系统异常：" + e.getMessage());
    }
}
