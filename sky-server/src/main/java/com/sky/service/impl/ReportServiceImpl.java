package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    WorkspaceService workspaceService;

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


    /*
     * 订单统计(指定时间区间)
     * */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
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

        //每一天订单数量:
        List<Integer> totalOrderlist = new ArrayList<>();//每一天的订单总数集合
        List<Integer> validOrderlist = new ArrayList<>(); //每一天的有效订单数集合
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//获取当前日期的最早时间, ex 2024-8-12 -> 2024-8-12 00:00:00
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//获取当前日期的最晚时间
            //查询每一天的订单总数 select count(*) from orders where orderTime > ? and orderTime < ?
            Integer totalOrderCount = getOrderCount(beginTime, endTime, null);
            totalOrderlist.add(totalOrderCount);

            //查询每一天的有效订单数 select count(*) from orders where orderTime > ? and orderTime < ? and status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);;
            validOrderlist.add(validOrderCount);
        }

        //订单总数:
        Integer totalOrderCount = totalOrderlist.stream().reduce(Integer::sum).get();

        //有效订单总数:
        Integer validOrderCount = validOrderlist.stream().reduce(Integer::sum).get();

        //订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
           orderCompletionRate =  validOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }

        //封装结果并返回
        return OrderReportVO.builder()
                .dateList(dateListStr)
                .orderCountList(StringUtils.join(totalOrderlist, ","))
                .validOrderCountList(StringUtils.join(validOrderlist, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /*
     * 查询指定时间区间,有效订单的菜品/套餐销量排名top10
     * */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//获取当前日期的最早时间, ex 2024-8-12 -> 2024-8-12 00:00:00
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);//获取当前日期的最晚时间

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> goodsNameList = salesTop10.stream().map(goods -> {
            return goods.getName();
        }).collect(Collectors.toList());//top10食品名字集合

        List<Integer> goodsSaleList = salesTop10.stream().map(goods -> {
            return goods.getNumber();
        }).collect(Collectors.toList());//top10食物销量集合

        //封装结果并返回
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(goodsNameList, ","))
                .numberList(StringUtils.join(goodsSaleList, ","))
                .build();
    }


    /*
     * 导出最近30天运营数据excel文件
     * */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库获取营业数据--查询最近30天营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);//获取当前日期的最早时间, ex 2024-8-12 -> 2024-8-12 00:00:00
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MIN);//获取当前日期的最晚时间
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);//查询营业概览数据

        //2.把查询到的数据写入excel文件中
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");//获取excel模板文件输入流对象
        try {
            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);//基于核板文件获得ExceL文件对象

            //填充数据--时间
            XSSFSheet sheet = excel.getSheet("Sheet1");//获取excel文件sheet页
            sheet.getRow(1).getCell(1).setCellValue("时间" + dateBegin + "至" + dateEnd);

            //填充数据--概览数据
            XSSFRow sheetRow = sheet.getRow(3);//获得第4行
            sheetRow.getCell(2).setCellValue(businessData.getTurnover());//填充营业额
            sheetRow.getCell(4).setCellValue(businessData.getOrderCompletionRate());//填充订单完成率
            sheetRow.getCell(6).setCellValue(businessData.getNewUsers());//填充新增用户数

            sheetRow = sheet.getRow(4);//获得第5行
            sheetRow.getCell(2).setCellValue(businessData.getValidOrderCount());//填充有效订单数
            sheetRow.getCell(4).setCellValue(businessData.getUnitPrice());//填充平均客单价

            //填充数据--明细数据
            int rowIndex = 7;
            LocalDate date = dateBegin;
            while(!date.isAfter(dateEnd)){
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                sheetRow = sheet.getRow(rowIndex++);
                sheetRow.getCell(1).setCellValue(date.toString()); //填充每一天的时间
                sheetRow.getCell(2).setCellValue(data.getTurnover());//填充每一天的营业额
                sheetRow.getCell(3).setCellValue(data.getValidOrderCount());//填充每一天的有效订单数
                sheetRow.getCell(4).setCellValue(data.getOrderCompletionRate());//填充每一天的订单完成率
                sheetRow.getCell(5).setCellValue(data.getUnitPrice());//填充每一天的平均客单价
                sheetRow.getCell(6).setCellValue(data.getNewUsers());//填充每一天的新增用户数


                date = date.plusDays(1);
            }


            //3.通过输出流,把excel文件下载到客户端浏览器
            ServletOutputStream responseOutputStream = response.getOutputStream();
            excel.write(responseOutputStream);

            //关闭资源
            resourceAsStream.close();
            responseOutputStream.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }








    /**
     * 根据条件统计订单数
     *
     * @param begin  起始时间
     * @param end    结束时间
     * @param status 订单状态
     * @return 订单数量
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        Integer count = orderMapper.countByMap(map);
        count = count == null ? 0 : count;

        return count;
    }

}
