package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {
    private final OrderCreateHelper orderCreateHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderDataMapper mapper;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        OrderCreatedEvent event = orderCreateHelper.persistOrder(command);
        log.info("Order is created with id: {}", event.getOrder().getId().getValue());
        CreateOrderResponse response = mapper.mapToCreateOrderResponse(event.getOrder());
        paymentOutboxHelper.savePaymentOutboxMessage(
                mapper.mapToOrderPaymentEventPayload(event),
                event.getOrder().getOrderStatus(),
                orderSagaHelper.orderStatusToSagaStatus(event.getOrder().getOrderStatus()),
                OutboxStatus.STARTED,
                UUID.randomUUID());

        log.info("Returning CreateOrderResponse with order id : {}", event.getOrder().getId());
        return response;
    }
}
