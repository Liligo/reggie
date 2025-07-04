package com.liligo.reggie.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
public class OrderStatusChangeEvent extends ApplicationEvent {
    // Getter方法
    private final Long orderId;
    private final Integer oldStatus;
    private final Integer newStatus;
    private final BigDecimal orderAmount;
    private final LocalDateTime orderTime;

    // 全参数构造函数
    public OrderStatusChangeEvent(Object source, Long orderId, Integer oldStatus,
                                  Integer newStatus, BigDecimal orderAmount, LocalDateTime orderTime) {
        super(source);
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.orderAmount = orderAmount;
        this.orderTime = orderTime;
    }
}