package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求分析:
 * 用户下单后可能存在的情况:
 * 1. 下单后未支付，订单一直处于“待支付”状态。
 * 2. 用户收货后管理端未点击完成按钮，订单一直处于“派送中”状态。
 */
/**
 * 定时任务逻辑:
 * 1. 每分钟检查一次是否存在支付超时订单。
 *    - 如果订单在下单后超过15分钟仍未支付，则判定为支付超时订单。
 *    - 对于支付超时的订单，修改其状态为“已取消”。
 * 2.   通过定时任务每天凌晨1点检查一次是否存在“派送中”的订单，如果存在则修改订单状态为“已完成"
 */

@Component //自定义定时任务类也需要实例化,交给ioc容器管理
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /*
    * 定时处理超时订单
    * */
    @Scheduled(cron = "0 0/2 * * * ?")//每分钟触发一次
    public void processTimeOutOrder(){
        log.info("定时处理超时订单" + LocalDateTime.now());

        //数据库查询所有超时订单
        // select * from orders where status = 待付款 and order_Time < (当前时间-15分钟)
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);//查询所有待付款,且下单时间超过15分钟的所有订单

        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);//更新状态为取消订单
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);//动态条件更新订单
            }
        }

    }


    /*
    * 定时处理一直处于"派送中"的订单
    * */
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点执行一次
    public void processDeliveryOrder(){
        log.info("定时处理一直处于\"派送中\"的订单");

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);//查询所有处于配送中状态,且下单时间超过1小时的订单

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED); // 修改订单状态为“已完成”
                orderMapper.update(orders);//动态条件更新订单
            }
        }


    }
}
