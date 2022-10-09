package com.food.ordering.system.order.test;

import com.food.ordering.system.order.test.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.test.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.test.mapper.OrderDataMapper;
import com.food.ordering.system.order.test.ports.output.repository.OrderRepository;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.payment.service.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackCommandHandler {
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> orderResult =
                orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if (orderResult.isEmpty()) {
            log.warn("Could not find order with tracking id: {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not find order with tracking id:" +
                    trackOrderQuery.getOrderTrackingId());
        }
        return orderDataMapper.mapToTrackOrderResponse(orderResult.get());
    }
}