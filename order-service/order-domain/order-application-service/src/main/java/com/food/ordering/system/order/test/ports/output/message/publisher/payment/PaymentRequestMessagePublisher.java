package com.food.ordering.system.order.test.ports.output.message.publisher.payment;

import com.food.ordering.system.order.test.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface PaymentRequestMessagePublisher {
    void publish(OrderPaymentOutboxMessage message, BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> callback);
}