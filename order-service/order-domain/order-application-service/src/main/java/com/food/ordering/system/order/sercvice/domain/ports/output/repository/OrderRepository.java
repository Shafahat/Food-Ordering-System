package com.food.ordering.system.order.sercvice.domain.ports.output.repository;

import com.food.ordering.system.order.sercvice.domain.entity.Order;
import com.food.ordering.system.order.sercvice.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findByTrackingId(TrackingId trackingId);
}
