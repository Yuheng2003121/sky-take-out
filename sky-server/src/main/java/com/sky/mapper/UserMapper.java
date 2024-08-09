package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
