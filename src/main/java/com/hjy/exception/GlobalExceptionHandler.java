package com.hjy.exception;

import com.hjy.common.lang.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j //日志
@RestControllerAdvice //定义全局控制器异常处理
public class GlobalExceptionHandler {


    /**
     * 全局异常处理
     * @param e
     * @return
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST) //返回状态码
    @ExceptionHandler(value = RuntimeException.class)//指定捕获Exception的各个类型异常
    public Result handle(RuntimeException e){
        log.error("RuntimeException:------------------>{}",e);
        return Result.fail(e.getMessage());
    }

    /**
     * Shiro异常处理
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED) //返回状态码
    @ExceptionHandler(value = ShiroException.class)//@ExceptionHandler表示针对性异常处理
    public Result handler(ShiroException e){
        log.error("ShiroException:------------------>{}",e);
        return Result.fail(401,"您尚未认证: 无权操作",null);
    }

    /**
     *  实体校验异常
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) //返回状态码
    @ExceptionHandler(value = MethodArgumentNotValidException.class)//@ExceptionHandler表示针对性异常处理
    public Result handler(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException:------------------>{}",e);
        //简化异常错误信息 ，不写下面的操作，异常信息会很多
        BindingResult bindingResult = e.getBindingResult();//拿到异常信息
        //可能会有多个异常，比如同时名称和邮箱都未填写，有第一个异常就抛出
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return Result.fail(objectError.getDefaultMessage());
    }
}
