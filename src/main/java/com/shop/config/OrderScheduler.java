package com.shop.config;

import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {

    @Autowired
    private OrderService orderService;

    /**
     * 每小时检查一次，自动取消超过3天未付款的订单
     */
    @Scheduled(fixedRate = 3600000)
    public void autoCancelExpiredOrders() {
        orderService.autoCancelExpiredOrders();
    }
}
