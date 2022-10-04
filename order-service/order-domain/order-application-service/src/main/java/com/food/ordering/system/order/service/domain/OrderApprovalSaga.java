package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, DomainEvent, OrderCancelledEvent> {
    private final OrderDomainService service;
    private final OrderSagaHelper helper;

    @Override
    @Transactional
    public DomainEvent process(RestaurantApprovalResponse response) {
        log.info("Approving order with id: {}", response.getOrderId());
        Order order = helper.findOrder(response.getOrderId());
        service.approveOrder(order);
        helper.saveOrder(order);
        log.info("Order with id: {} is approved", order.getId().getValue());
        return new EmptyEvent();
    }

    @Override
    @Transactional
    public OrderCancelledEvent rollback(RestaurantApprovalResponse response) {
        log.info("Cancelling order with id: {}", response.getOrderId());
        Order order = helper.findOrder(response.getOrderId());
        OrderCancelledEvent event = service.cancelOrderPayment(order, response.getFailureMessages());
        helper.saveOrder(order);
        log.info("Order with id: {} is cancelling", event.getOrder().getId().getValue());
        return event;
    }
}
