package com.sky.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /*
     * 新增菜品和对应的口味
     * */
    @Override
    @Transactional //事务管理注解,要么全成功要么全失败
    public void saveWIthFlavor(DishDTO dishDTO) {
        //重新赋值
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入1条dish菜品数据
        dishMapper.insert(dish);

        //获取insert sql返回主键到dish里的id的值, 也就是新增dish菜品在数据库的主键id
        Long dishId = dish.getId();

        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /*
     * 菜品分页查询
     * */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }


    /*
     * 菜品批量删除
     * */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //不能删除
        //判断当前菜品是否能够删除----是否为起售中的菜品? 起售中(status=1)则不能删
        for (Long dishId : ids) {
            Dish dish = dishMapper.getById(dishId);

            //当前菜品不能删除
            //判断当前菜品是否能够删除----是否为起售中的菜品? 起售中(status=1)则不能删
            if(dish.getStatus() == StatusConstant.ENABLE){
                //当前菜品处于起售中,不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

            //判断当前菜品是否能够删除----是否被套餐关联? 关联了则不可以删除
            List<Long> setmealIds = setMealDishMapper.getSetmealIdsByDishId(dishId);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }

            //当前菜品可以删除
            dishMapper.deleteById(dishId);

            //删除菜品表关联的口味数据
            dishFlavorMapper.deleteByDishId(dishId);

        }


    }


}
