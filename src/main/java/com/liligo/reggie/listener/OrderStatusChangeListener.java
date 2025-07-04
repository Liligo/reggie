package com.liligo.reggie.listener;

import com.liligo.reggie.event.OrderStatusChangeEvent;
import com.liligo.reggie.service.SalesSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Slf4j
@Component
public class OrderStatusChangeListener {

    @Autowired
    private SalesSummaryService salesSummaryService;

    @Async
    @EventListener
    @CacheEvict(value = "salesSummary", allEntries = true)
    public void handleOrderStatusChange(OrderStatusChangeEvent event) {
        log.info("收到订单状态变更事件: 订单ID={}, 旧状态={}, 新状态={}, 金额={}",
                event.getOrderId(), event.getOldStatus(), event.getNewStatus(), event.getOrderAmount());

        try {
            // 添加空值检查
            if (event.getOrderTime() == null || event.getOrderAmount() == null) {
                log.warn("订单状态变更事件缺少必要信息: 订单时间或金额为空");
                return;
            }

            LocalDate orderDate = event.getOrderTime().toLocalDate();
            Integer oldStatus = event.getOldStatus();
            Integer newStatus = event.getNewStatus();

            // 如果是新订单创建
            if (oldStatus == null || oldStatus == 0) {
                salesSummaryService.handleOrderCreated(orderDate, event.getOrderAmount());
            }

            // 如果订单完成
            if (newStatus == 5 && (oldStatus == null || oldStatus != 5)) {
                salesSummaryService.handleOrderCompleted(orderDate, event.getOrderAmount());
            }

        } catch (Exception e) {
            log.error("处理订单状态变更事件失败: {}", e.getMessage(), e);
        }
    }
}