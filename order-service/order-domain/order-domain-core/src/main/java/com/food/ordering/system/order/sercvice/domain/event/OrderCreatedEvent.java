package com.food.ordering.system.order.sercvice.domain.event;

import com.food.ordering.system.order.sercvice.domain.entity.Order;

import java.time.ZonedDateTime;

public final class OrderCreatedEvent extends OrderEvent {
    public OrderCreatedEvent(Order order, ZonedDateTime createAt) {
        super(order, createAt);
    }
}
