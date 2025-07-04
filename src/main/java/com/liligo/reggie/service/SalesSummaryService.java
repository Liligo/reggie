package com.liligo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liligo.reggie.dto.SalesSummaryDto;
import com.liligo.reggie.entity.SalesSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SalesSummaryService extends IService<SalesSummary> {

    /**
     * 根据时间范围获取销售统计
     */
    SalesSummaryDto getSalesSummary(String startTime, String endTime);

    /**
     * 获取今日销售统计
     */
    SalesSummaryDto getTodaySalesSummary();

    /**
     * 获取本周销售统计
     */
    SalesSummaryDto getWeeklySalesSummary();

    /**
     * 获取本月销售统计
     */
    SalesSummaryDto getMonthlySalesSummary();

    SalesSummaryDto getSalesSummaryByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 处理订单创建统计
     */
    void handleOrderCreated(LocalDate orderDate, BigDecimal amount);

    /**
     * 处理订单完成统计
     */
    void handleOrderCompleted(LocalDate orderDate, BigDecimal amount);

    /**
     * 获取或创建统计记录
     */
    SalesSummary getOrCreateSummary(LocalDate date, String type);

    SalesSummaryDto getAllSalesSummary();
}