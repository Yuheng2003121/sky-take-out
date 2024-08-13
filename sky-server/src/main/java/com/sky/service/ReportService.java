package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /*
     * 统计营业额(指定时间区间)
     * */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);


}
