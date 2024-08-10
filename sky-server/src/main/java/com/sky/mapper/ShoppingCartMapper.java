package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /*
     * 条件查询
     * */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /*
    * 根据购物车id修改商品数量
    * */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart existedCart);


    /*
    * 新增购物车数据
    * */
    @Insert("INSERT INTO shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "VALUES (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /*
     * 清空用户所有购物车(根据当前登录的微信用户id)
     * */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);



    /*
    * 删除用户的一个购物车(根据购物车id)
    * */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
