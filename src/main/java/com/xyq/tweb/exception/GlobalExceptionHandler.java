package com.xyq.tweb.exception;

import com.xyq.tweb.domain.web.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * 全局异常处理
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Void> handleGlobalException(Exception e) {
        e.printStackTrace();
        String errStr = e.getMessage();
        if (StringUtils.isEmpty(errStr)) {
            StackTraceElement[] stackTraceElement = e.getStackTrace();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 10 && i < stackTraceElement.length; i++) {
                StackTraceElement stackElement = stackTraceElement[i];
                stringBuilder.append(stackElement.getFileName())
                        .append(".")
                        .append(stackElement.getClassName())
                        .append(".")
                        .append(stackElement.getMethodName())
                        .append(".")
                        .append(stackElement.getLineNumber())
                        .append(";");
            }
            errStr = stringBuilder.toString();
        }
        return Result.error(e + "->" + errStr);
    }

    /**
     * 客户端参数异常处理
     */
    @ExceptionHandler(value = BadRequestException.class)
    public Result<Void> HandleBadRequestException(BadRequestException e) {
        log.warn("请求端错误-{}", e.getMsg());
        return Result.badRequest(e.getMsg());
    }

    /**
     * 客户端TOKEN认证异常处理
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    public Result<Void> HandleAuthFailedException(UnauthorizedException e) {
        log.warn("TOKEN认证授权错误-{}", e.getMsg());
        return Result.badRequest(e.getMsg());
    }

    /**
     * 请求体过大异常处理
     * nginx 默认限制是 1MB, tomcat 默认限制为 2MB
     * spring.servlet.multipart.max-file-size: 1024 * 1024 * 2
     */
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public Result<Void> HandleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("请求超出最大限制, {}", e.getMessage());
        return Result.badRequest(String.format("请求超出最大限制, 最大请求体%s", e.getMaxUploadSize()));
    }

    /**
     * 请求参数缺失
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<Void> HandleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少requestParam参数, {}", e.getMessage());
        return Result.badRequest(String.format("缺少请求参数%s, 类型%s", e.getParameterName(), e.getParameterType()));
    }

    /**
     * 参数校验失败
     */
    @ExceptionHandler(value = BindException.class)
    public Result<Void> HandleBindException(BindException e) {
        e.printStackTrace();
        return Result.badRequest(e.getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 参数校验失败
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Void> HandleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        return Result.badRequest(fieldError.getField() + fieldError.getDefaultMessage());

    }

    /**
     * 参数校验失败
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<Void> HandleConstraintViolationException(ConstraintViolationException e) {
        e.printStackTrace();
        return Result.badRequest(e.getConstraintViolations().toArray(new ConstraintViolation[0])[0].getMessage());
    }

}
