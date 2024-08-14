package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;


    /*
     * 统计营业额(指定时间区间)
     * */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //每一天的时间数据:
        //集合存放时间区间里的每一天的日期(从begin到end)
        List<LocalDate> dateList = new ArrayList<>();
        /*LocalDate time = begin;
        while (!time.isAfter(end)) {
            dateList.add(time);
            time = time.plusDays(1);
        }*/
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算,计算指定日期的后一天对应的臼期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListStr = StringUtils.join(dateList, ",");//把集合拼成字符串逗号分隔

        //每一天的营业额数据:
        List<Double> turnOverList = dateList.stream().map(date -> {
            //查询该date日期对应的营业额(也就是状态为"已完成"的订单金额合计)
            // select sum(amount)from orders where order_time > ? and order_time< ? and statuL = 5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//获取当前日期的最早时间, ex 2024-8-12 -> 2024-8-12 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//获取当前日期的最晚时间

            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnOver = orderMapper.sumByMap(map); //当前符合条件订单的营业额

            return turnOver == null ? 0.0 :turnOver;//如果 turnOver 为 null，则返回 0

        }).collect(Collectors.toList());//把每一个map的数据封装成集合
        String turnOverListStr = StringUtils.join(turnOverList, ",");//把集合拼成字符串逗号分隔


        //封装结果并返回
        return TurnoverReportVO.builder()
                .dateList(dateListStr)
                .turnoverList(turnOverListStr)
                .build();
    }


    /*
     * 统计用户(指定时间区间)
     * */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //每一天的时间数据:
        //集合存放时间区间里的每一天的日期(从begin到end)
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算,计算指定日期的后一天对应的臼期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        String dateListStr = StringUtils.join(dateList, ",");//把集合拼成字符串逗号分隔

        List<Integer> newUserList = new ArrayList<>(); //每一天的新增用户数量  select count(id) from user where create_time < ? and create_time > ?
        List<Integer> totalUserList = new ArrayList<>();//每一天的用户总量 select count(id) from user where create_time <
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//获取当前日期的最早时间, ex 2024-8-12 -> 2024-8-12 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//获取当前日期的最晚时间

            Map map1 = new HashMap<>();
            map1.put("begin", beginTime);
            map1.put("end", endTime);
            Integer newUserCount = userMapper.countByMap(map1);//每一天的新增用户数量
            newUserList.add(newUserCount);

            Map map2 = new HashMap<>();
            map2.put("end", endTime);
            Integer totalUserCount = userMapper.countByMap(map2);//每一天的用户总量
            totalUserList.add(totalUserCount);
        }


        //封装结果并返回
        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }


}
