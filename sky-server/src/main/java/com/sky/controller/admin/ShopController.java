package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController") //指定该类bean对象的名字(以防止跟user包下的ShopController冲突)
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    RedisTemplate redisTemplate;//注入redis数据库对象

    /*
    * 设置店铺营业状态
    * status 1:营业  0:打样
    * */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态为:{}", status == 1 ? "营业中" : "打样");

        //使用redis 进行string类型添加操作
        redisTemplate.opsForValue().set("SHOP_STATUS", status);

        return Result.success();
    }

    /*
    * 查询店铺营业状态
    * */
    @GetMapping("/status")
    @ApiOperation("管理端获取店铺的营业状态")
    public Result<Integer> getStatus(){
        //从redis数据库中查询key为SHOP_STATUS的value
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");

        log.info("管理端获取店铺的营业状态为:{}", status == 1 ? "营业中" : "打样");

        return Result.success(status);
    }



}
