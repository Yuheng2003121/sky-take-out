package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Controller;

/*操作order表*/

@Mapper
public interface OrderMapper {

    /*
    * 向订单表插入一条数据
    * */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);


    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /*
     * 历史订单查询(分页)
     * */
    Page<Orders> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 查询订单(根据订单id)
    * */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    /*
     * 管理端订单搜索(分页)
     * */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    * 根据状态，分别查询出待接单、待派送、派送中的订单数量
    * */
    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Integer status);
}
