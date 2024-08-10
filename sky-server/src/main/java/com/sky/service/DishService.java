package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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


    /*
     * 查询单个菜品和对应的口味(根据id)
     * */
    DishVO getByIdWithFalvor(Long id);


    /*
     *
     * 修改菜品以及关联该菜品id的口味
     * */
    void updateWithFlavor(DishDTO dishDTO);


    /*
     * 根据分类id查询菜品
     * */
    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /*
     * 起售停售菜品
     * */
    void startOrStop(Integer status, Long id);
}
