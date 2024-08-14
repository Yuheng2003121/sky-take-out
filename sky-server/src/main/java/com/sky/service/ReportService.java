package com.sky.service;

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

}
