package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*操作order_detail订单明细表 (订单表跟订单明细表的关系是一对多)*/

@Mapper
public interface OrderDetailMapper {

    /*
    * 批量插入订单明细数据
    * */
    void insertBatch(List<OrderDetail> orderDetailList);

    /*
    * 根据订单id查询所有订单明细数据
    * */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
