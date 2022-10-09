package com.food.ordering.system.order.service.data.access.outbox.payment.mapper;

import com.food.ordering.system.order.service.data.access.outbox.payment.entity.PaymentOutboxEntity;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxDataAccessMapper {

    public PaymentOutboxEntity mapToOutboxEntity(OrderPaymentOutboxMessage message) {
        return PaymentOutboxEntity.builder()
                .id(message.getId())
                .sagaId(message.getSagaId())
                .createdAt(message.getCreatedAt())
                .type(message.getType())
                .payload(message.getPayload())
                .orderStatus(message.getOrderStatus())
                .sagaStatus(message.getSagaStatus())
                .outboxStatus(message.getOutboxStatus())
                .version(message.getVersion())
                .build();
    }

    public OrderPaymentOutboxMessage mapToOrderPaymentOutboxMessage(PaymentOutboxEntity entity) {
        return OrderPaymentOutboxMessage.builder()
                .id(entity.getId())
                .sagaId(entity.getSagaId())
                .createdAt(entity.getCreatedAt())
                .type(entity.getType())
                .payload(entity.getPayload())
                .orderStatus(entity.getOrderStatus())
                .sagaStatus(entity.getSagaStatus())
                .outboxStatus(entity.getOutboxStatus())
                .version(entity.getVersion())
                .build();
    }
}
