package com.sky.aspect;


/*
* 自定义AOP切面类, 实现自动填充处理逻辑
* */

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@Aspect //当前类不是普通类, 而是AOP类
@Component
public class AutoFillAspect {


    //定义切入点:对哪些方法进行处理

    //com.sky.mapper这个包下所有类的所有方法 同时必须是标注了@AutoFill自定义注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}


    //定义通知:对切面点方法进行额外处理
    //前置通知, 需要对切面方法进行公共字段赋值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("AOP开始进行公共字段的自动填充...");

        //1. 获取当前被拦截的方法上的注解标注的value(枚举类定义的数据库操作类型)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型, 也就是注解上的value


        //2. 获取当前被拦截的切面的方法上的实体参数对象
        Object[] args = joinPoint.getArgs();

        if(args == null || args.length == 0){//以防止传入mapper方法参数上没有值
            return;
        }

        Object entity = args[0];//实体对象

        //3. 准备为实体参数对象要赋的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4. 根据不同标注的value进行不同的赋值操作: 通过反射获取实体对象的方法 -> 再进行方法调用赋值
        if(operationType == OperationType.INSERT){
            //为4个公共字段都赋值

            //获得实体对象的set和get方法
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                //通过反射为实体对象赋值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值

            //获得实体对象的set和get方法
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);//也可以通过常量引用方法名
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为实体对象赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }


    }


}
