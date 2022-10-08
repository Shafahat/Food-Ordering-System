package com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurant_approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.function.BiConsumer;

public interface RestaurantApprovalRequestMessagePublisher {
    void publish(OrderApprovalOutboxMessage message, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> callback);
}
