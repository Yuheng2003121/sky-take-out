package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /*
     * 新增菜品(注意,需要返回插入的菜品主键id)
     * */
    @AutoFill(value = OperationType.INSERT) //AOP自动填充字段
    void insert(Dish dish);


    /*
    * 菜品分页查询(需要动态sql判断)
    * */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
}
