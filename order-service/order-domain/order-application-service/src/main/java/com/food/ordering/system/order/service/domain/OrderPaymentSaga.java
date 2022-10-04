package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, DomainEvent> {
    private final OrderDomainService service;
    private final OrderSagaHelper helper;

    @Override
    @Transactional
    public OrderPaidEvent process(PaymentResponse response) {
        log.info("Completing payment for order wth id: {}", response.getOrderId());
        Order order = helper.findOrder(response.getOrderId());
        OrderPaidEvent event = service.payOrder(order);
        helper.saveOrder(order);
        log.info("Order with id: {} is paid", event.getOrder().getId().getValue());
        return event;
    }

    @Override
    @Transactional
    public DomainEvent<?> rollback(PaymentResponse response) {
        log.info("Cancelling order with id: {}", response.getOrderId());
        Order order = helper.findOrder(response.getOrderId());
        service.cancelOrder(order, response.getFailureMessages());
        helper.saveOrder(order);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
        return new EmptyEvent();
    }

}
