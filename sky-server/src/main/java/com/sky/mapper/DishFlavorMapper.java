package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /*
    * 批量插入菜品口味(动态sql)
    * */
    void insertBatch(List<DishFlavor> flavors);


    /*
    * 删除口味(根据菜品dish id)
    * */
    @Delete("delete from sky_take_out.dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);


    /*
     * 删除口味(根据菜品dish id集合) 优化一个一个删除
     * */
    void deleteByDishIds(List<Long> dishIds);


    /*
    * 根据菜品dish id查询对应的口味
    * */
    @Select("select * from sky_take_out.dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
