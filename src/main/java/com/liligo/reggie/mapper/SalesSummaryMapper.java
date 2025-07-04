package com.liligo.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liligo.reggie.dto.SalesSummaryDto;
import com.liligo.reggie.entity.SalesSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface SalesSummaryMapper extends BaseMapper<SalesSummary> {

    /**
     * 根据时间范围查询销售统计（从orders表实时查询）
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COALESCE(SUM(amount), 0) as totalSales, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN 1 ELSE 0 END) as validOrders, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END) as validSales " +
            "FROM orders " +
            "WHERE order_time BETWEEN #{startTime} AND #{endTime}")
    SalesSummaryDto getSalesSummaryByDateRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 获取全部销售统计（从orders表实时查询）
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COALESCE(SUM(amount), 0) as totalSales, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN 1 ELSE 0 END) as validOrders, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END) as validSales " +
            "FROM orders")
    SalesSummaryDto getAllSalesSummary();

    /**
     * 获取今日销售统计（从orders表实时查询）
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COALESCE(SUM(amount), 0) as totalSales, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN 1 ELSE 0 END) as validOrders, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END) as validSales " +
            "FROM orders " +
            "WHERE DATE(order_time) = CURDATE()")
    SalesSummaryDto getTodaySalesSummary();

    /**
     * 获取本周销售统计（从orders表实时查询）
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COALESCE(SUM(amount), 0) as totalSales, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN 1 ELSE 0 END) as validOrders, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END) as validSales " +
            "FROM orders " +
            "WHERE YEARWEEK(order_time, 1) = YEARWEEK(CURDATE(), 1)")
    SalesSummaryDto getWeeklySalesSummary();

    /**
     * 获取本月销售统计（从orders表实时查询）
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COALESCE(SUM(amount), 0) as totalSales, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN 1 ELSE 0 END) as validOrders, " +
            "SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END) as validSales " +
            "FROM orders " +
            "WHERE YEAR(order_time) = YEAR(CURDATE()) AND MONTH(order_time) = MONTH(CURDATE())")
    SalesSummaryDto getMonthlySalesSummary();

    /**
     * 从缓存表查询统计数据
     */
    @Select("SELECT " +
            "SUM(total_orders) as totalOrders, " +
            "SUM(total_sales) as totalSales, " +
            "SUM(valid_orders) as validOrders, " +
            "SUM(valid_sales) as validSales " +
            "FROM sales_summary " +
            "WHERE summary_date BETWEEN #{startDate} AND #{endDate} " +
            "AND summary_type = #{summaryType}")
    SalesSummaryDto getSalesSummaryFromCache(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("summaryType") String summaryType);

    /**
     * 增加总订单数和总销售额
     */
    @Update("UPDATE sales_summary SET " +
            "total_orders = total_orders + 1, " +
            "total_sales = total_sales + #{amount}, " +
            "update_time = NOW() " +
            "WHERE summary_date = #{date} AND summary_type = #{type}")
    int incrementTotalStats(@Param("date") LocalDate date,
                            @Param("type") String type,
                            @Param("amount") BigDecimal amount);

    /**
     * 增加有效订单数和有效销售额
     */
    @Update("UPDATE sales_summary SET " +
            "valid_orders = valid_orders + 1, " +
            "valid_sales = valid_sales + #{amount}, " +
            "update_time = NOW() " +
            "WHERE summary_date = #{date} AND summary_type = #{type}")
    int incrementValidStats(@Param("date") LocalDate date,
                            @Param("type") String type,
                            @Param("amount") BigDecimal amount);
}