package com.hjy.common.lang;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果集的统一封装
 */
@Data
public class Result implements Serializable {
    private Integer code;//200表示正常，非200表示异常
    private String message;
    private Object data;

    //成功
    public static  Result  success(Object data){
        return success(200,"成功",data);
    }
    public static  Result  success(Integer code,String message,Object data){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    //失败
    public static  Result  fail(String message){
        return success(400,message,null);
    }

    public static  Result  fail(Integer code,String message,Object data){
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

}
