package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /*
    * 用户下单
    * */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);


    /*
     * 历史订单查询(分页)
     * */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
     * 查看订单详情(根据订单id)
     * */
    OrderVO details(Long id);

    /*
     * 取消订单(根据订单id)
     * */
    void userCancelById(Long id);

    /*
     * 再来一单
     * */
    void repetition(Long id);

    /*
     * 订单搜索(分页)
     * */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
     * 各个状态的订单数量统计
     * */
    OrderStatisticsVO statistics();

    /**
     * 接单(根据订单id)
     *
     * @return
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     *
     * @return
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     *
     * @return
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);


    /**
     * 完成订单
     *
     * @return
     */
    void complete(Long id);


    /*
     * 客户催单(使用websocket)
     * */
    void reminder(Long id);

    /*
     * 派送订单
     * */
    void delivery(Long id);
}
