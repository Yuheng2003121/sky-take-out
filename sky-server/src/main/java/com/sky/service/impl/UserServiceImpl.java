package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    WeChatProperties weChatProperties;
    
    @Autowired
    UserMapper userMapper;


    /*
    * 微信登录(向微信官方服务器接口发送请求)
    * */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        //1.向微信官方服务器接口发送请求, 获取当前用户的openId
        //微信登录接口文档参考:https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
        String openId = getOpenId(userLoginDTO.getCode());

        //2.判断返回的openId是否为空
        // 如果openId为空则登陆失败抛出业务异常
        if(openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //3.openId不为空,判断当前openId是否在数据库里存在
        User user = userMapper.getByOpenId(openId);
        // 不存在则为新用户-> 为该用户注册(数据库新增该用户)
        if(user == null){
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insertUser(user);
        }

        //返回用户对象
        return user;
    }

    private String getOpenId(String code){
        //1.向微信官方服务器接口发送请求, 获取当前用户的openId
        //微信登录接口文档参考:https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
        Map<String, String> wxLoginRequestMap = new HashMap<>();
        wxLoginRequestMap.put("appId", weChatProperties.getAppid());
        wxLoginRequestMap.put("secret",weChatProperties.getSecret());
        wxLoginRequestMap.put("js_code", code);
        wxLoginRequestMap.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, wxLoginRequestMap); //接收返回结果

        //解析返回来的结果成json对象
        JSONObject jsonObject = JSON.parseObject(json);
        String openId = jsonObject.getString("openid");//获取返回来的结果的openId属性

        return openId;
    }

}
