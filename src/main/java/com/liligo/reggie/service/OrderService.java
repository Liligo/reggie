package com.liligo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liligo.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 提交订单
     *
     * @param orders 订单信息
     */
    void submit(Orders orders);
}
