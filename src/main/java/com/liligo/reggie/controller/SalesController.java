package com.liligo.reggie.controller;

import com.liligo.reggie.common.Result;
import com.liligo.reggie.dto.SalesSummaryDto;
import com.liligo.reggie.service.SalesSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SalesSummaryService salesSummaryService;

    /**
     * 获取销售额统计信息（按时间范围）
     */
    @GetMapping("/summary")
    public Result<SalesSummaryDto> getSalesSummary(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            SalesSummaryDto summary = salesSummaryService.getSalesSummary(startTime, endTime);
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取销售额统计信息失败: {}", e.getMessage());
            return Result.error("获取销售额统计信息失败");
        }
    }

    /**
     * 获取今日销售统计
     */
    @GetMapping("/today")
    public Result<SalesSummaryDto> getTodaySalesSummary() {
        try {
            SalesSummaryDto summary = salesSummaryService.getTodaySalesSummary();
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取今日销售统计失败: {}", e.getMessage());
            return Result.error("获取今日销售统计失败");
        }
    }

    /**
     * 获取本周销售统计
     */
    @GetMapping("/weekly")
    public Result<SalesSummaryDto> getWeeklySalesSummary() {
        try {
            SalesSummaryDto summary = salesSummaryService.getWeeklySalesSummary();
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取本周销售统计失败: {}", e.getMessage());
            return Result.error("获取本周销售统计失败");
        }
    }

    /**
     * 获取本月销售统计
     */
    @GetMapping("/monthly")
    public Result<SalesSummaryDto> getMonthlySalesSummary() {
        try {
            SalesSummaryDto summary = salesSummaryService.getMonthlySalesSummary();
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取本月销售统计失败: {}", e.getMessage());
            return Result.error("获取本月销售统计失败");
        }
    }

    @GetMapping("/all")
    public Result<SalesSummaryDto> getSalesSummaryByDate() {
        try {
            SalesSummaryDto summary = salesSummaryService.getAllSalesSummary();
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取指定日期销售统计失败: {}", e.getMessage());
            return Result.error("获取指定日期销售统计失败");
        }
    }
}