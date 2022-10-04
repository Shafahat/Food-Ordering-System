package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.DomainEvent;
import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurant_approval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.payment.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, DomainEvent> {
    private final OrderDomainService service;
    private final OrderRepository repository;
    private final OrderPaidRestaurantRequestMessagePublisher publisher;

    @Override
    @Transactional
    public OrderPaidEvent process(PaymentResponse response) {
        log.info("Completing payment for order wth id: {}", response.getOrderId());
        Order order = findOrder(response.getOrderId());
        OrderPaidEvent event = service.payOrder(order);
        repository.save(order);
        log.info("Order with id: {} is paid", event.getOrder().getId().getValue());
        return event;
    }

    @Override
    @Transactional
    public DomainEvent<?> rollback(PaymentResponse response) {
        log.info("Cancelling order with id: {}", response.getOrderId());
        Order order = findOrder(response.getOrderId());
        service.cancelOrder(order, response.getFailureMessages());
        repository.save(order);
        log.info("Order with id: {} is cancelled", order.getId().getValue());
        return new EmptyEvent();
    }

    private Order findOrder(String orderId) {
        return repository.findById(new OrderId(UUID.fromString(orderId)))
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " could not be found!"));
    }
}
