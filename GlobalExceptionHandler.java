package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author li
 * 定义全局异常处理器（就是个代理类，用于增强Controller类）
 *处理Controller层的异常
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})//定义只要该项目中的类加了RestController和Controller注解，就会被该处理器处理
@ResponseBody//接收前端的请求体中json数据
@Slf4j
public class GlobalExceptionHandler {

    /**
     处理输入重复账号的异常（前端传回的账号已存在）
     * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.error(exception.getMessage());

        if(exception.getMessage().contains("Duplicate entry")){//唯一约束
            String[] split = exception.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return R.error(msg);
        }

        return R.error("失败了");
    }

    /**
     * 处理输入重复账号的异常（前端传回的账号已存在）
     * */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception){
        log.error(exception.getMessage());
        
        return R.error(exception.getMessage());
    }

}
