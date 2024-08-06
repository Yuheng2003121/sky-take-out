package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetMealDishMapper setmealDishMapper;

    /*
     * 新增套餐还有对应的setMeal_dish中间表
     * */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //1.在setmeal表里新增套餐(还需获取到新增套餐的主键id)
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);


        //2.在setmeal_dish中间表里新增与之关联的dish和setmeal
        Long setMealId = setmeal.getId();//刚刚新增套餐在数据库的主键id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setMealId);
        });

        //保存套餐和菜品的关联关系(在setmeal_dish中间表新增与之对应的套餐和菜品)
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /*
     * 套餐分页查询
     * */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<Setmeal> setmealPage = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }
}
