package com.sky.service;


import com.sky.dto.SetmealDTO;

public interface SetMealService {


    /*
     * 新增套餐还有对应的setMeal_dish中间表
     * */
    void saveWithDish(SetmealDTO setmealDTO);
}
