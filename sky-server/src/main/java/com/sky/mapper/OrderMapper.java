package com.sky.mapper;


import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Controller;

/*操作order表*/

@Mapper
public interface OrderMapper {

    /*
    * 向订单表插入一条数据
    * */
    void insert(Orders orders);
}
