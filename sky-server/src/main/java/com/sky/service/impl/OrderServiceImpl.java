package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /*
     * 用户下单
     * */
    @Override
    @Transactional //事务注解, 事务回滚, 方法有错误就回滚事务(要么全成功,要么全失败)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //1.处理业务异常(传来的地址簿为空, 购物车数据为空)

        //1.1根据地址簿id查询地址簿数据, 判断地址簿是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //如果传来的地址簿为空,抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //1.2查询当前用户所有购物车数据(根据用户id), 判断该用户的购物车数据是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);//根据用户id查询所有购物车数据

        if(shoppingCartList == null || shoppingCartList.size() == 0){
            //如果购物车数据为空, 抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2.向订单表插入一条数据(一条订单数据)
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);//属性拷贝
        //设置其他属性
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));//设置订单号
        orders.setPhone(addressBook.getPhone());//设置手机号(从上面查到的地址簿中取)
        orders.setConsignee(addressBook.getConsignee());//设置收货人(从上面查到的地址簿中取)
        orders.setUserId(userId);
        orderMapper.insert(orders);//插入一条订单(插入后返回了在该新增数据在数据库的主键id到该对象上)

        //3.向订单明细表插入n条数据(因为一个订单包含用户购物车中的多个菜品/套餐) ,订单表跟订单明细表的关系是一对多
        List<OrderDetail> orderDetailList = new ArrayList<>();
        shoppingCartList.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();//订单明细实体
            BeanUtils.copyProperties(cart, orderDetail);//把每一个购物车数据属性复制到orderDetail实体
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetailList);//像订单明细表插入所有订单明细的数据


        //4.用户下单成功后,清空该用户所有购物车数据
        shoppingCartMapper.deleteByUserId(userId);


        //5.封装OrderSubmitVO返回该结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }
}
