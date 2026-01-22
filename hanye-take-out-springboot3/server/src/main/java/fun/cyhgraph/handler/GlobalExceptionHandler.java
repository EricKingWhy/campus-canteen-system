package fun.cyhgraph.handler;

import fun.cyhgraph.constant.MessageConstant;
import fun.cyhgraph.exception.BaseException;
import fun.cyhgraph.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice(basePackages = "fun.cyhgraph.controller")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result excepitonHandler(BaseException ex) {
        log.info("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        log.error("SQL完整性约束违规异常: {}", message);

        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            // 【核心修复】修正拼写：ALREADY_EXiST -> ALREADY_EXIST
            String msg = username + MessageConstant.ALREADY_EXIST;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 捕获所有未知的异常
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception ex) {
        log.error("未知的异常信息：", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
