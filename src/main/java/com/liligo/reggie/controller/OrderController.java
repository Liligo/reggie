package com.liligo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.Orders;
import com.liligo.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    // 分页查询订单
    @GetMapping("/page")
    public Result<Page<Orders>> page(int page, int pageSize,
                                     @RequestParam(required = false) String number,
                                     @RequestParam(required = false) String beginTime,
                                     @RequestParam(required = false) String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(orders.getUserId() != null, Orders::getUserId, orders.getUserId());
        queryWrapper.like(StringUtils.isNotBlank(number), Orders::getNumber, number);

        // 日期范围查询
        // 日期参数处理说明：前端传入的日期参数需要URL编码（空格转为%20，冒号转为%3A）
        // 后端会自动解码并转换为字符串参数，如： beginTime=2025-07-28%2000%3A00%3A00 → beginTime=2025-07-28 00:00:00
        // 使用Java 8的DateTime API进行日期解析和比较
        if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(beginTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            queryWrapper.between(Orders::getOrderTime, start, end);
        }

        return Result.success(orderService.page(pageInfo, queryWrapper));
    }

    // 个人用户分页查询订单
    @GetMapping("/userPage")
    public Result<Page<Orders>> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);

        return Result.success(orderService.page(pageInfo, queryWrapper));
    }

    // 提交订单
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("提交订单: {}", orders);
        // 调用服务层方法处理订单提交逻辑
        orderService.submit(orders);

        return Result.success("订单提交成功");
    }

    private String getStatusMessage(Integer status) {
        return switch (status) {
            case 1 -> "订单已确认";
            case 2 -> "订单正在派送";
            case 3 -> "订单已派送";
            case 4 -> "订单已完成";
            case 5 -> "订单已取消";
            default -> "订单状态更新成功";
        };
    }
    // 新增状态更新接口（支持派送操作）
    @PutMapping
    public Result<String> updateStatus(@RequestBody Orders orders) {
        log.info("更新订单状态: orderId={}, status={}", orders.getId(), orders.getStatus());
        // 执行状态更新
        orderService.updateStatus(orders.getId(), orders.getStatus());
        String message = getStatusMessage(orders.getStatus());
        return Result.success(message);
    }
}