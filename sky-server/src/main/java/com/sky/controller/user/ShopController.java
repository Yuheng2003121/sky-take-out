package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController") //指定该类bean对象的名字(以防止跟admin包下的ShopController冲突)
@RequestMapping("/user/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    @Autowired
    RedisTemplate redisTemplate;//注入redis数据库对象

    /*
    * 查询店铺营业状态
    * */
    @GetMapping("/status")
    @ApiOperation("用户端获取店铺的营业状态")
    public Result<Integer> getStatus(){
        //从redis数据库中查询key为SHOP_STATUS的value
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");

        log.info("用户端获取店铺的营业状态为:{}", status == 1 ? "营业中" : "打样");

        return Result.success(status);
    }



}
