package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

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

    /*
     *起售停售套餐
     * */
    void startOrStop(Integer status, Long id);


    /*
     * 根据套餐id查询套餐(和与之关联的菜品)
     * */
    SetmealVO getByIdWithDish(Long id);


    /*
     * 修改套餐
     * */
    void update(SetmealDTO setmealDTO);
}
