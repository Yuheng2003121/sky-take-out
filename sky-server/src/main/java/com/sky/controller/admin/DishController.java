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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /*
    * 新增菜品和对应的口味
    * */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);

        dishService.saveWIthFlavor(dishDTO);

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





}
