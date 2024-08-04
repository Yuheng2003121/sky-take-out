package com.sky.annotation;

/*
* 自定义注解:该自定义注解用于被单独AOP处理的方法 (用于标识某个方法需要进行功能字段自动填充处理)
* */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//该注解只能加到方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    //指定数据库操作类型,用该注解时候,需要标注value = OperationType枚举类里的某个值
    OperationType value();
}
