package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {

    /*
     * 新增菜品和对应的口味
     * */
    void saveWIthFlavor(DishDTO dishDTO);



    /*
    * 菜品分页查询
    * */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
     * 菜品批量删除
     * */
    void deleteBatch(List<Long> ids);
}
