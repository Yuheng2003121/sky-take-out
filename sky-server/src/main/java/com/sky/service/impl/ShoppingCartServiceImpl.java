package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    /*
     * 添加购物车
     * */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入到购物车中的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();//拦截器会在用户登录成功后把用户id放到线程, 使用该方法获取
        shoppingCart.setUserId(userId);//设置userId

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//获取购物车

        //如果商品在购物车已经存在, 只需将该购物车商品数量+1
        if(list != null && list.size() > 0){
            ShoppingCart existedCart = list.get(0);//因为list只可能有1条或0条数据, 可以这样直接获取那个购物车实体
            existedCart.setNumber(existedCart.getNumber() + 1);//给该购物车上的商品 数量 + 1;
            shoppingCartMapper.updateNumberById(existedCart);


        }else {//如果商品在购物车不存在, 则需要插入该商品(新增购物车)

            //检查当前传来的购物车里的商品是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();

            if(dishId != null){
                //本次添加到购物车的商品是菜品, 获取菜品数据 -> 新增菜品到购物车

                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());////设置该购物车的商品名字
                shoppingCart.setImage(dish.getImage());//设置该购物车的商品图片
                shoppingCart.setAmount(dish.getPrice());//设置该购物车的商品价格

                shoppingCart.setNumber(1);//设置该购物车的商品数量
                shoppingCart.setCreateTime(LocalDateTime.now());//设置该购物车的创建时间


            }else {
                //本次添加到购物车的的商品是套餐, 获取菜品套餐->添加套餐到购物车

                Setmeal setmeal = setmealMapper.getById(setmealId);

                shoppingCart.setName(setmeal.getName());////设置该购物车的商品名字
                shoppingCart.setImage(setmeal.getImage());//设置该购物车的商品图片
                shoppingCart.setAmount(setmeal.getPrice());//设置该购物车的商品价格

                shoppingCart.setNumber(1);//设置该购物车的商品数量
                shoppingCart.setCreateTime(LocalDateTime.now());//设置该购物车的创建时间
            }

            shoppingCartMapper.insert(shoppingCart);
        }
    }


    /*
     * 查看该微信用户所有购物车(根据userId)
     * */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();//拦截器会在用户登录成功后把用户id放到线程, 使用该方法获取
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }


    /*
     * 清空用户购物车
     * */
    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();//拦截器会在用户登录成功后把用户id放到线程, 使用该方法获取

        shoppingCartMapper.deleteByUserId(userId);
    }


    /*
     * 删除购物车中一个商品
     * */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        Long userId = BaseContext.getCurrentId();//拦截器会在用户登录成功后把用户id放到线程, 使用该方法获取
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);//条件查询购物车

        ShoppingCart cart = shoppingCartList.get(0);//获得购物车实体, 因为list只可能有1条或0条数据


        //查询该购物车的商品的数量是否大于1
        if(cart.getNumber() > 1){ //该购物车的商品的数量大于1 -> 更新该商品的数量-1
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart);

        }else if(cart.getNumber() == 1){//该购物车的商品的数量=1 -> 删除该购物车数据
            shoppingCartMapper.deleteById(cart.getId());
        }



    }
}
