package com.food.ordering.system.order.service.data.access.order.adapter;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.data.access.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.data.access.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import com.food.ordering.system.order.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpaRepository;
    private final OrderDataAccessMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.mapToOrder(jpaRepository.save(mapper.mapToOrderEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::mapToOrder);
    }

    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return jpaRepository.findByTrackingId(trackingId.getValue()).map(mapper::mapToOrder);
    }
}
