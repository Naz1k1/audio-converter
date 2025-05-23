package com.naz1k1.handler;
import com.naz1k1.model.response.ApiError;
import com.naz1k1.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     *
     * @param e 验证异常
     * @return ResponseEntity 包含错误信息的响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("参数验证失败");
        
        return ResponseEntity.badRequest()
                .body(new ApiError("验证错误", message));
    }

    /**
     * 处理输入验证异常
     *
     * @param e 输入验证异常
     * @return ResponseEntity 包含错误信息的响应
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiError> handleInvalidInputException(InvalidInputException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError("输入验证失败", e.getMessage()));
    }

    /**
     * 处理转换异常
     *
     * @param e 转换异常
     * @return ResponseEntity 包含错误信息的响应
     */
    @ExceptionHandler(ConversionException.class)
    public ResponseEntity<ApiError> handleConversionException(ConversionException e) {
        log.error("转换失败", e);
        return ResponseEntity.internalServerError()
                .body(new ApiError("转换失败", "视频转换过程中发生错误"));
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常
     * @return ResponseEntity 包含错误信息的响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception e) {
        log.error("未处理的异常", e);
        return ResponseEntity.internalServerError()
                .body(new ApiError("系统错误", "服务器内部错误"));
    }
}