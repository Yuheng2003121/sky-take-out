package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /*
    * 根据微信登录接口返回openId判断是该openId否存在
    * */
    @Select("select * from sky_take_out.user where openid = #{openId}")
    User getByOpenId(String openId);

    /*
    * 新增用户
    * */
    void insertUser(User user);

    @Select("select * from user where id = #{userId} ")
    User getById(Long userId);

    /*
    * 动态条件统计用户数量
    * */
    Integer countByMap(Map map);
}
