package com.liligo.reggie.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liligo.reggie.common.BaseContext;
import com.liligo.reggie.entity.*;
import com.liligo.reggie.event.OrderStatusChangeEvent;
import com.liligo.reggie.mapper.OrderMapper;
import com.liligo.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户ID：{}", userId);

        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 获取当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartQueryWrapper);

        // 如果购物车为空，抛出异常
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法提交订单");
        }

        // 向订单表中插入订单数据
        // 查询用户信息
        User user = userService.getById(userId);
        log.info("用户信息：{}", user);
        // 查询地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new RuntimeException("地址信息不存在，无法提交订单");
        }

        long orderId = IdWorker.getId(); // 生成订单ID

        // 计算订单总金额
        // AtomicInteger 是 Java 中的一个原子整数类，它提供了一种线程安全的方式来操作整数。
        AtomicInteger amount = new AtomicInteger(0);

        // 计算订单总金额, 同时输出订单明细
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setAmount(shoppingCart.getAmount());
            // 计算订单金额
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetailList.add(orderDetail);
        }

        // 填充订单信息
        orders.setId(orderId);  // 设置订单ID
        orders.setUserId(userId);   // 设置用户ID
        orders.setUserName(user.getName());
        orders.setNumber(String.valueOf(orderId)); // 生成订单号
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);    // 设置订单状态为已完成
        orders.setAmount(new BigDecimal(amount.get())); // 计算订单金额
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);

        // 向订单明细表中插入订单明细数据
        orderDetailService.saveBatch(orderDetailList);

        // 清空购物车数据
        shoppingCartService.remove(shoppingCartQueryWrapper);

        // 发布订单创建事件
        OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                this,
                orders.getId(),
                null,   // 新订单，旧状态为null
                orders.getStatus(), // 新状态
                orders.getAmount(),
                orders.getOrderTime()
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * 验证状态变更是否合法
     */
    private void validateStatusChange(Integer oldStatus, Integer newStatus) {
        // 取消订单的验证
        if (newStatus == 5) {
            if (oldStatus != 1 && oldStatus != 2) {
                throw new RuntimeException("订单状态不允许取消");
            }
        }

        // 其他状态变更的验证逻辑
        // 可以根据业务需求添加更多验证规则
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        // 根据ID查询订单
        Orders orders = this.getById(id);

        if(orders == null) {
            throw new RuntimeException("订单不存在");
        }

        // 记录旧状态
        Integer oldStatus = orders.getStatus();
        validateStatusChange(oldStatus, status);

//        // 验证状态合法性（根据新常量）
//        if (!Stream.of(STATUS_PENDING_PAYMENT, STATUS_TO_BE_DELIVERED,
//                        STATUS_DELIVERED, STATUS_COMPLETED, STATUS_CANCELLED)
//                .anyMatch(s -> s.equals(status))) {
//            throw new OrderNotFoundException("无效的订单状态: " + status);
//        }
//
//        // 更新状态流转逻辑（示例规则）
//        String currentStatus = orders.getStatus();
//        if (STATUS_TO_BE_DELIVERED.equals(currentStatus)) {
//            if (!STATUS_DELIVERED.equals(status) && !STATUS_CANCELLED.equals(status)) {
//                throw new OrderNotFoundException("待派送订单只能变更为已派送或已取消");
//            }
//        } else if (STATUS_DELIVERED.equals(currentStatus)) {
//            if (!STATUS_COMPLETED.equals(status)) {
//                throw new OrderNotFoundException("已派送订单只能变更为已完成");
//            }
//        }
        log.info("更新订单状态 ID: {} | 旧状态: {} → 新状态: {}", id, oldStatus, status);

        // 更新状态并记录日志
        orders.setStatus(status);
        this.updateById(orders);

        // 发布订单状态变更事件
        OrderStatusChangeEvent event = new OrderStatusChangeEvent(
                this,
                id,
                oldStatus,
                status,
                orders.getAmount(),
                orders.getOrderTime()
        );
        eventPublisher.publishEvent(event);
    }
}
