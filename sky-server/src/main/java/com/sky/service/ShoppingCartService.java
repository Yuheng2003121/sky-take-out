package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /*
     * 添加购物车
     * */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);



    /*
     * 查看该微信用户所有购物车(根据userId)
     * */
    List<ShoppingCart> showShoppingCart();

    /*
     * 清空用户购物车
     * */
    void cleanShoppingCart();
}
