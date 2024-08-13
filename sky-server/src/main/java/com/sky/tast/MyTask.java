package com.sky.tast;

/*自定义定时任务类*/

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component //自定义定时任务类也需要实例化,交给ioc容器管理
@Slf4j
public class MyTask {

    /**
     * 开启定时任务每隔5秒触发一次
     */
   /* @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        log.info("定时任务开始执行: {}", new Date());
    }*/




}
