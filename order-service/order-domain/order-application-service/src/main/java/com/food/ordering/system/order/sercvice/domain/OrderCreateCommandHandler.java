package com.food.ordering.system.order.sercvice.domain;

import com.food.ordering.system.order.sercvice.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.sercvice.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.sercvice.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.sercvice.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.sercvice.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {
    private final OrderCreateHelper orderCreateHelper;
    private final OrderDataMapper orderDataMapper;
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
        orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
        return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(),
                "Order created successfully");
    }
}
