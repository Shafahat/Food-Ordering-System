package com.food.ordering.system.payment.service.domain.event;

import com.food.ordering.system.payment.service.domain.entity.Order;

import java.time.ZonedDateTime;

public final class OrderPaidEvent extends OrderEvent {
    public OrderPaidEvent(Order order, ZonedDateTime createAt) {
        super(order, createAt);
    }
}
