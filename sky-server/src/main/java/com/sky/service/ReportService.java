package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
     * 统计营业额(指定时间区间)
     * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


    /*
     * 统计用户(指定时间区间)
     * */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);


    /*
     * 订单统计(指定时间区间)
     * */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /*
     * 菜品/套餐销量排名top10
     * */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
