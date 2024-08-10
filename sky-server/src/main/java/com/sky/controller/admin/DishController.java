package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
* 菜品管理
* */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;//注入redis数据库bean对象

    /*
    * 新增菜品和对应的口味
    * */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);

        dishService.saveWIthFlavor(dishDTO);

        //mysal数据库新增完后清除redis数据库缓存数据,所以下次用户访问根据cateID查询菜品时可以添加最新数据到redis数据库
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    /*
    * 菜品分页查询
    * */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){//是query不是body, 不需要@RequestBody
        log.info("菜品分页查询:{}", dishPageQueryDTO);

        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /*
    * 菜品批量删除
    * Query 参数:
            ids    必需
            string
            菜品id，之间用逗号分隔
            示例值:1,2,3
    * */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){//@RequestParam 可以自动解析 1,2,3用逗号分隔的字符串,并解析到list里
        log.info("菜品批量删除:{}", ids);

        dishService.deleteBatch(ids);

        //mysql数据库菜品删除后, 删除redis数据库全部数据
        cleanCache("dish_*");//删除redis数据库里所有key以dish_*开头的数据

        return Result.success();
    }

    /*
    * 查询单个菜品和对应的口味(根据id), 用于修改菜品时的页面回显
    * */
    @GetMapping("/{id}")
    @ApiOperation("查询单个菜品根据id")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询单个菜品根据id: {}", id);
        DishVO dishVO = dishService.getByIdWithFalvor(id);

        return Result.success(dishVO);
    }


    /*
    * 修改菜品以及关联该菜品id的口味
    * */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}", dishDTO);

        dishService.updateWithFlavor(dishDTO);

        //mysql数据库菜品修改后, 删除redis数据库全部数据
        cleanCache("dish_*");//删除redis数据库里所有key以dish_*开头的数据


        return Result.success();
    }

    /*
    * 起售停售菜品
    * */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        // 调用服务层方法处理业务逻辑
        dishService.startOrStop(status, id);

        // 菜品起售停售后,清理所有以 "dish_" 开头的缓存数据
        cleanCache("dish_*");//删除redis数据库里所有key以dish_*开头的数据

        // 返回操作结果
        return Result.success();
    }

    /*
    * 根据分类id查询菜品
    * */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类id查询菜品:{}", categoryId);

        List<Dish> list = dishService.list(categoryId);

        return Result.success(list);
    }


    /*
    * 清理缓存数据()
    * */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }



}
