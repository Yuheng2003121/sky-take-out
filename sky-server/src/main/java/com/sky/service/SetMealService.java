package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetMealService {


    /*
     * 新增套餐还有对应的setMeal_dish中间表
     * */
    void saveWithDish(SetmealDTO setmealDTO);

    /*
     * 套餐分页查询
     * */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /*
     * 批量删除套餐
     * */
    void deleteBatch(List<Long> ids);
}
