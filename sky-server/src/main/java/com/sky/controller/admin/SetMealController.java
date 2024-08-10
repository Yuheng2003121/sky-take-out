package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

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

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetMealController {

    @Autowired
    SetMealService setMealService;


    /*
    * 新增套餐还有对应的setMeal_dish中间表
    * */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")//删除缓存,如果方法接收的setmealDTO的categoryId为100, 那么在redis删除的key: setMealCache::100
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);

        setMealService.saveWithDish(setmealDTO);

        return Result.success();
    }

    /*
    * 套餐分页查询
    * */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}", setmealPageQueryDTO);

        PageResult pageResult = setMealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    /*
    * 批量删除套餐
    * - 可以一次删除一个套餐，也可以批量删除套餐
       - 起售中的套餐不能删除
    * */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true) //删除所有setmealCache下的缓存数据
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐:{}",ids);

        setMealService.deleteBatch(ids);

        return Result.success();
    }


    /*
    * 根据套餐id查询套餐(和与之关联的菜品) (用于修改套餐的页面回显)
    * */
    @GetMapping("/{id}")
    @ApiOperation("根据套餐id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据套餐id查询套餐 id:{}", id);

        SetmealVO setmealVO = setMealService.getByIdWithDish(id);

        return Result.success(setmealVO);
    }

    /*
    * 修改套餐
    * */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true) //删除所有setmealCache下的缓存数据
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}", setmealDTO);

        setMealService.update(setmealDTO);

        return Result.success();

    }



    /*
    *起售停售套餐
    * */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true) //删除所有setmealCache下的缓存数据
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("起售停售套餐 id:{}, {}",id, status );

        setMealService.startOrStop(status, id);

        return Result.success();
    }


}
