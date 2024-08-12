package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("code", "ORDERPAID");

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
       
        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        
        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        order.setStatus(OrderStatus);
        order.setPayStatus(OrderPaidStatus);
        order.setCheckoutTime(check_out_time);

        orderMapper.update(order);
        
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
     * 历史订单查询(分页)
     * */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.historyOrders(ordersPageQueryDTO);

        // 查询出订单明细，并封装入OrderVO进行响应
        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);//根据订单id查询所有订单明细数据
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), list);
    }

    /*
     * 查看订单详情(根据订单id)
     * */
    @Override
    public OrderVO details(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;

    }

    /*
     * 取消订单(根据订单id)
     * */
    @Override
    public void userCancelById(Long id) {
        /*- 待支付和待接单状态下，用户可直接取消订单
        - 商家已接单状态下，用户取消订单需电话沟通商家
        - 派送中状态下，用户取消订单需电话沟通商家
        - 如果在待接单状态下取消订单，需要给用户退款
        - 取消订单后需要将订单状态修改为“已取消”*/

        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){

            //支付状态修改为 退款
            ordersDB.setPayStatus(Orders.REFUND);
        }

        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason("用户取消");
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.update(ordersDB);//更新订单状态

    }

    /*
     * 再来一单
     * */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单全部详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        //List<ShoppingCart> shoppingCartList = new ArrayList<>();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");//忽略id属性拷贝
            shoppingCart.setUserId(BaseContext.getCurrentId());//设置userId
            shoppingCart.setCreateTime(LocalDateTime.now());//设置创建时间

            return shoppingCart;

        }).collect(Collectors.toList());

        //将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }




}

