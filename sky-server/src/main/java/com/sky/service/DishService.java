package com.sky.service;


import com.sky.dto.DishDTO;

public interface DishService {

    /*
     * 新增菜品和对应的口味
     * */
    void saveWIthFlavor(DishDTO dishDTO);
}
