package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


/**
 * Spring Cache 常用注解:
 *
 * 1. @EnableCaching
 *    - 说明: 开启缓存注解功能，通常加在启动类上
 *
 * 2. @Cacheable
 *    - 说明: 在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据;
 *            如果没有缓存数据，调用方法并将方法返回值放到缓存中
 *
 * 3. @CachePut
 *    - 说明: 将方法的返回值放到缓存中
 *
 * 4. @CacheEvict
 *    - 说明: 将一条或多条数据从缓存中删除
 */

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetMealService setmealService;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId") //新增缓存,如果方法接收的categoryId为100,在方法执行前先查询redis缓存中是否有数据, key: setMealCache::100，如果有数据，则直接返回缓存数据;如果没有数据, 调用方法并将方法返回值放到该key的缓存中
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("根据分类id查询套餐:{}",categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        log.info("根据套餐id查询包含的菜品列表:{}",id);
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}
