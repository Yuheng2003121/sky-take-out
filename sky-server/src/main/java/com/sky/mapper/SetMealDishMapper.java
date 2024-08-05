package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /*
     * 根据菜品id集合查询对应的套餐id(动态sql)
     * */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


    /*
     * 根据菜品id集合查询对应的套餐id
     * */
    @Select("select * from sky_take_out.setmeal_dish where dish_id = #{id}")
    List<Long> getSetmealIdsByDishId(Long id);







}
