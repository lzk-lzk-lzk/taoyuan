package com.example.peach.common.exception;

import com.example.peach.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
// 全局异常处理
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    // 处理业务异常
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 处理请求体参数校验异常
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("，"));
        log.warn("请求参数校验异常: {}", message, e);
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    // 处理表单参数绑定异常
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("，"));
        log.warn("表单参数绑定异常: {}", message, e);
        return Result.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    // 处理普通参数校验异常
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("普通参数校验异常: {}", e.getMessage(), e);
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    // 处理认证异常
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证异常: {}", e.getMessage(), e);
        return Result.fail(401, "账号或密码错误");
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    // 处理无权限异常
    public Result<Void> handleAccessDeniedException(Exception e) {
        log.warn("无权限访问: {}", e.getMessage(), e);
        return Result.fail(403, "权限不足");
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    // 处理数据库锁等待异常
    public Result<Void> handleCannotAcquireLockException(CannotAcquireLockException e) {
        log.warn("数据库锁等待超时: {}", e.getMessage(), e);
        return Result.fail(409, "请求处理中，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    // 处理其它未捕获异常
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统异常：" + e.getMessage());
    }
}
