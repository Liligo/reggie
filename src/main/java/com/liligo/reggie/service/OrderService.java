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

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status  新状态
     */
    void updateStatus(Long orderId, Integer status);
}
