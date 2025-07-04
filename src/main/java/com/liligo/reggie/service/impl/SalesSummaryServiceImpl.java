package com.liligo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liligo.reggie.dto.SalesSummaryDto;
import com.liligo.reggie.entity.SalesSummary;
import com.liligo.reggie.mapper.SalesSummaryMapper;
import com.liligo.reggie.service.SalesSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Service
public class SalesSummaryServiceImpl extends ServiceImpl<SalesSummaryMapper, SalesSummary> implements SalesSummaryService {
    // 添加统一的日期格式
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public SalesSummaryDto getSalesSummary(String startTime, String endTime) {
        LocalDateTime start, end;

        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            start = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER);
            end = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER);
        }else {
            return getTodaySalesSummary();
        }

        // 优先从缓存获取
        SalesSummaryDto summary = getSummaryFromCache(start, end);
        if (summary == null || summary.getTotalOrders() == null || summary.getTotalOrders() == 0) {
            // 缓存没有数据，从orders表实时查询
            summary = getSalesSummaryByDateRange(start, end);
        }

        calculateDerivedFields(summary);
        summary.setStartTime(start);
        summary.setEndTime(end);

        return summary;
    }

    @Override
    @Cacheable(value = "salesSummary", key = "'today'", unless = "#result == null")
    public SalesSummaryDto getTodaySalesSummary() {
        try {
            // 直接从orders表查询今日数据
            SalesSummaryDto summary = baseMapper.getTodaySalesSummary();

            if (summary == null) {
                summary = new SalesSummaryDto();
            }

            // 计算衍生字段
            calculateDerivedFields(summary);

            return summary;
        } catch (Exception e) {
            log.error("获取今日销售统计失败", e);
            throw new RuntimeException("获取今日销售统计失败");
        }
    }

    @Override
    @Cacheable(value = "salesSummary", key = "'weekly'", unless = "#result == null")
    public SalesSummaryDto getWeeklySalesSummary() {
        try {
            // 直接从orders表查询本周数据
            SalesSummaryDto summary = baseMapper.getWeeklySalesSummary();

            if (summary == null) {
                summary = new SalesSummaryDto();
            }

            // 计算衍生字段
            calculateDerivedFields(summary);

            return summary;
        } catch (Exception e) {
            log.error("获取本周销售统计失败", e);
            throw new RuntimeException("获取本周销售统计失败");
        }
    }

    @Override
    @Cacheable(value = "salesSummary", key = "'monthly'", unless = "#result == null")
    public SalesSummaryDto getMonthlySalesSummary() {
        try {
            // 直接从orders表查询本月数据
            SalesSummaryDto summary = baseMapper.getMonthlySalesSummary();

            if (summary == null) {
                summary = new SalesSummaryDto();
            }

            // 计算衍生字段
            calculateDerivedFields(summary);

            return summary;
        } catch (Exception e) {
            log.error("获取本月销售统计失败", e);
            throw new RuntimeException("获取本月销售统计失败");
        }
    }

    @Override
    @Cacheable(value = "salesSummary", key = "'range_' + #startTime.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMdd')) + '_' + #endTime.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMdd'))", unless = "#result == null")
    public SalesSummaryDto getSalesSummaryByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("从数据库查询自定义范围销售统计: {} - {}", startTime, endTime);
            SalesSummaryDto summary = baseMapper.getSalesSummaryByDateRange(startTime, endTime);

            if (summary == null) {
                summary = new SalesSummaryDto();
            }

            // 计算衍生字段
            calculateDerivedFields(summary);

            log.info("自定义范围销售统计: {}", summary);
            return summary;
        } catch (Exception e) {
            log.error("获取自定义范围销售统计失败", e);
            throw new RuntimeException("获取自定义范围销售统计失败");
        }
    }

    @Override
    @Transactional
    public void handleOrderCreated(LocalDate orderDate, BigDecimal amount) {
        try {
            // 确保记录存在
            getOrCreateSummary(orderDate, "daily");

            // 更新统计
            int result = baseMapper.incrementTotalStats(orderDate, "daily", amount);
            if (result == 0) {
                log.warn("更新订单创建统计失败，日期: {}, 金额: {}", orderDate, amount);
            }
        } catch (Exception e) {
            log.error("处理订单创建统计失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void handleOrderCompleted(LocalDate orderDate, BigDecimal amount) {
        try {
            // 确保记录存在
            getOrCreateSummary(orderDate, "daily");

            // 更新统计
            int result = baseMapper.incrementValidStats(orderDate, "daily", amount);
            if (result == 0) {
                log.warn("更新订单完成统计失败，日期: {}, 金额: {}", orderDate, amount);
            }
        } catch (Exception e) {
            log.error("处理订单完成统计失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SalesSummary getOrCreateSummary(LocalDate date, String type) {
        LambdaQueryWrapper<SalesSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesSummary::getSummaryDate, date)
                .eq(SalesSummary::getSummaryType, type);

        SalesSummary summary = getOne(queryWrapper);
        if (summary == null) {
            summary = new SalesSummary();
            summary.setSummaryDate(date);
            summary.setSummaryType(type);
            save(summary);
        }
        return summary;
    }

    @Override
    @Cacheable(value = "salesSummary", key = "'all'", unless = "#result == null")
    public SalesSummaryDto getAllSalesSummary() {
        try {
            // 直接从orders表查询全部数据
            SalesSummaryDto summary = baseMapper.getAllSalesSummary();

            if (summary == null) {
                summary = new SalesSummaryDto();
            }

            // 计算衍生字段
            calculateDerivedFields(summary);

            return summary;
        } catch (Exception e) {
            log.error("获取全部销售统计失败", e);
            throw new RuntimeException("获取全部销售统计失败");
        }
    }

    /**
     * 从缓存获取统计数据
     */
    private SalesSummaryDto getSummaryFromCache(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        return baseMapper.getSalesSummaryFromCache(startDate, endDate, "daily");
    }

    /**
     * 计算衍生字段
     */
    private void calculateDerivedFields(SalesSummaryDto summary) {
        if (summary == null) {
            return;
        }

        // 确保基础字段不为null
        if (summary.getTotalOrders() == null) summary.setTotalOrders(0);
        if (summary.getTotalSales() == null) summary.setTotalSales(BigDecimal.ZERO);
        if (summary.getValidOrders() == null) summary.setValidOrders(0);
        if (summary.getValidSales() == null) summary.setValidSales(BigDecimal.ZERO);

        // 计算平均订单金额
        if (summary.getValidOrders() > 0) {
            summary.setAvgOrderAmount(
                    summary.getValidSales().divide(
                            BigDecimal.valueOf(summary.getValidOrders()),
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        } else {
            summary.setAvgOrderAmount(BigDecimal.ZERO);
        }

        // 计算订单完成率
        if (summary.getTotalOrders() > 0) {
            summary.setOrderCompletionRate(
                    summary.getValidOrders().doubleValue() / summary.getTotalOrders() * 100
            );
        } else {
            summary.setOrderCompletionRate(0.0);
        }
    }
}