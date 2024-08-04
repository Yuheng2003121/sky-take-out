package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /*
    * 捕获 sql异常
    * */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String errMsg = ex.getMessage();
        if(errMsg.contains("Duplicate entry")){
            //Duplicate entry 'zhangsan' for key 'employee.idx_username'
            String username = errMsg.split(" ")[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);

        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

    }

    /**
     * 捕获 分类删除不允许异常
     * @param ex
     * @return
     */
    @ExceptionHandler(DeletionNotAllowedException.class)
    public Result exceptionHandler(DeletionNotAllowedException ex){
        log.error("删除不允许异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获综合业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


}
