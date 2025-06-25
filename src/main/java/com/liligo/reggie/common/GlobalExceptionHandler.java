package com.liligo.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;

import java.sql.SQLIntegrityConstraintViolationException;


/**
 * 全局异常处理器
 */
@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    /**
     * 处理SQL完整性约束异常
     *
     * @return 返回错误结果
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exception(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")){
            String message = ex.getMessage().split(" ")[2 ] + "已存在";
            return Result.error(message);
        }
        return Result.error("未知错误");
    }

    /**
     * 处理分类删除异常
     *
     * @return 返回错误结果
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> exception(CustomException ex){
        log.error("异常信息：{}", ex.getMessage());

        return Result.error(ex.getMessage());
    }
}
