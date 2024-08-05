package com.sky.config;


import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
* 配置类, 用于创建AliOss对象
* */
@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean //优化:当IOC容器没有AliOssUtil类的bean对象时才创建
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){//@Compoent注解的类作为@Bean注解的方法可以直接让该类注入IOC容器,并获取bean对象
        log.info("开始创建阿里云文件上传工具类对象:{}", aliOssProperties);
        AliOssUtil aliOssUtil = new AliOssUtil(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());

        return aliOssUtil;
    }
}
