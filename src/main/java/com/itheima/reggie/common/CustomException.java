package com.itheima.reggie.common;

import java.time.temporal.ChronoUnit;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException{
     public CustomException(String message){
         super(message);
     }
}
