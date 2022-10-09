package com.food.ordering.system.order.test.ports.output.repository;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(OrderId id);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
