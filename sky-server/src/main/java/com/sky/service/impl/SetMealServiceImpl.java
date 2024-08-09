package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetMealDishMapper setmealDishMapper;

    @Autowired
    DishMapper dishMapper;

    /*
     * 新增套餐还有对应的setMeal_dish中间表
     * */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //1.在setmeal表里新增套餐(还需获取到新增套餐的主键id)
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);


        //2.在setmeal_dish中间表里新增与之关联的dish和setmeal
        Long setMealId = setmeal.getId();//刚刚新增套餐在数据库的主键id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setMealId);
        });

        //保存套餐和菜品的关联关系(在setmeal_dish中间表新增与之对应的套餐和菜品)
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /*
     * 套餐分页查询
     * */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<Setmeal> setmealPage = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }

    /*
     * 批量删除套餐
     * */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //先检查每一个套餐是否为起售中, 起售中的套餐不能删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);//调用根据套餐id查询套餐
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //每一个套餐都不为起售中,可以删除
        //批量删除套餐(根据id集合)
        setmealMapper.deleteBatch(ids);

        //批量删除菜品套餐关系表中的套餐数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }


    /*
     *起售停售套餐
     * */
    @Override
    public void startOrStop(Integer setmealStatus, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(setmealStatus == StatusConstant.ENABLE){
            List<Dish> dishList = dishMapper.getBySetmealId(id);//根据套餐id查询setmeal_dish关系表对应的所有菜品dish

            if(dishList != null && dishList.size() > 0){//该套餐id有对应的菜品
                dishList.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){//该套餐有菜品的状态为停售
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        //该套餐关联的所有菜品为启售,该套餐可以起售
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(setmealStatus)
                .build();

        setmealMapper.update(setmeal);

    }


    /*
     * 根据套餐id查询套餐(和与之关联的菜品)
     * */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //先根据套餐id在中间表查询
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        //再根据套餐id查询套餐
        Setmeal setmeal = setmealMapper.getById(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;

    }

    /*
     * 修改套餐
     * */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //1.更新套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //2.先删除setmeal_dish关系表与之套餐id关联的全部数据
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);

        //更新传来的数据里的套餐菜品关系集合中关联的套餐id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //3.重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }



    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
