package com.liligo.reggie.controller;


import com.liligo.reggie.common.Result;
import com.liligo.reggie.entity.Orders;
import com.liligo.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders) {
        log.info("提交订单: {}", orders);
        // 调用服务层方法处理订单提交逻辑
        orderService.submit(orders);

        return Result.success("订单提交成功");
    }
}