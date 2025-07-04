package com.liligo.reggie.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesSummaryDto implements Serializable {

    // 查询时间范围
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 统计结果
    private Integer totalOrders;
    private BigDecimal totalSales;
    private Integer validOrders;
    private BigDecimal validSales;

    // 平均订单金额
    private BigDecimal avgOrderAmount;

    // 订单完成率
    private Double orderCompletionRate;

    public SalesSummaryDto() {
        this.totalOrders = 0;
        this.totalSales = BigDecimal.ZERO;
        this.validOrders = 0;
        this.validSales = BigDecimal.ZERO;
        this.avgOrderAmount = BigDecimal.ZERO;
        this.orderCompletionRate = 0.0;
    }
}