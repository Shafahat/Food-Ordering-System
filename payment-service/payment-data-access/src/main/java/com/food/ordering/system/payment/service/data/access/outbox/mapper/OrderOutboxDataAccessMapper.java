package com.food.ordering.system.payment.service.data.access.outbox.mapper;

import com.food.ordering.system.payment.service.data.access.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxDataAccessMapper {
    public OrderOutboxEntity mapToOutboxEntity(OrderOutboxMessage message) {
        return OrderOutboxEntity.builder()
                .id(message.getId())
                .sagaId(message.getSagaId())
                .createdAt(message.getCreatedAt())
                .type(message.getType())
                .payload(message.getPayload())
                .outboxStatus(message.getOutboxStatus())
                .paymentStatus(message.getPaymentStatus())
                .version(message.getVersion())
                .build();
    }

    public OrderOutboxMessage mapToOrderOutboxMessage(OrderOutboxEntity entity) {
        return OrderOutboxMessage.builder()
                .id(entity.getId())
                .sagaId(entity.getSagaId())
                .createdAt(entity.getCreatedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .outboxStatus(entity.getOutboxStatus())
                .paymentStatus(entity.getPaymentStatus())
                .version(entity.getVersion())
                .build();
    }
}
