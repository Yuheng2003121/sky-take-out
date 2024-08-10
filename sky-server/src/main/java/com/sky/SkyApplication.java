package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * Spring Cache 常用注解:
 *
 * 1. @EnableCaching
 *    - 说明: 开启缓存注解功能，通常加在启动类上
 *
 * 2. @Cacheable
 *    - 说明: 在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据;
 *            如果没有缓存数据，调用方法并将方法返回值放到缓存中
 *
 * 3. @CachePut
 *    - 说明: 将方法的返回值放到缓存中
 *
 * 4. @CacheEvict
 *    - 说明: 将一条或多条数据从缓存中删除
 */

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@EnableCaching //开启缓存注解功能，通常加在启动类上
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
