package org.xuanyuan.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.common.result.Result;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result<?> handleBaseException(BaseException e) {
        log.error("Business exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("JSON parse error: {}", e.getMessage());
        return Result.error(400, "JSON 格式错误，请检查请求体（例如是否多漏了逗号）");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 针对网关常见的 404 图标问题，降低日志级别或简化返回
        if (e.getMessage() != null && e.getMessage().contains("favicon.ico")) {
            return Result.error(404, "资源不存在");
        }
        log.error("Unexpected exception: ", e);
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
