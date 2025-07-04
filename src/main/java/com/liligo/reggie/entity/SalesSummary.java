package com.liligo.reggie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sales_summary")
public class SalesSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // 统计日期
    private LocalDate summaryDate;

    // 统计类型：daily, weekly, monthly
    private String summaryType;

    // 总订单数
    private Integer totalOrders;

    // 总销售额
    private BigDecimal totalSales;

    // 有效订单数（已完成的订单）
    private Integer validOrders;

    // 有效销售额
    private BigDecimal validSales;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    public SalesSummary() {
        this.totalOrders = 0;
        this.totalSales = BigDecimal.ZERO;
        this.validOrders = 0;
        this.validSales = BigDecimal.ZERO;
    }
}