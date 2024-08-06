package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    /*
    *插入套餐
    * */
    @AutoFill(value = OperationType.INSERT) //AOP自动填充字段
    void insert(Setmeal setmeal);

    /*
     * 套餐分页查询(动态sql,因为需要判断传来的字段是否为空)
     * */
    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /*
    * 查询套餐(根据id)
    * */
    @Select("select * from setmeal where id = #{setmealId}")
    Setmeal getById(Long setmealId);


    /*
     * 批量删除套餐(根据套餐id集合)
     * */
    void deleteBatch(List<Long> ids);


    /*
     *修改套餐(动态sql,因为需要判断传来的字段是否为空)
     * */
    @AutoFill(value = OperationType.INSERT) //AOP自动填充字段
    void update(Setmeal setmeal);
}
