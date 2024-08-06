package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
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
     * 根据菜品id查询对应的套餐id
     * */
    @Select("select * from sky_take_out.setmeal_dish where dish_id = #{id}")
    List<Long> getSetmealIdsByDishId(Long id);


    /*
    * 新增菜品和套餐的关系在中间表(根据套餐集合)
    * */
    void insertBatch(List<SetmealDish> setmealDishes);


    /*
     * 批量删除菜品套餐关系表中的数据(根据套餐id集合)
     * */
    void deleteBySetmealIds(List<Long> setMealIds);

    /*
     * 批量删除菜品套餐关系表中的数据(根据套餐id)
     * */
    @Delete("delete from sky_take_out.setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);


    /*
    * 根据套餐id查询套餐菜品关系
    * */
    @Select("select * from sky_take_out.setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);


}
