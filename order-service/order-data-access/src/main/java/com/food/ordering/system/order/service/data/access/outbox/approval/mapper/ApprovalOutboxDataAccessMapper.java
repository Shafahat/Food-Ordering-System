package com.food.ordering.system.order.service.data.access.outbox.approval.mapper;

import com.food.ordering.system.order.service.data.access.outbox.approval.entity.ApprovalOutboxEntity;
import com.food.ordering.system.order.service.outbox.model.approval.OrderApprovalOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class ApprovalOutboxDataAccessMapper {

    public ApprovalOutboxEntity mapToOutboxEntity(OrderApprovalOutboxMessage message) {
        return ApprovalOutboxEntity.builder()
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

    public OrderApprovalOutboxMessage mapToOrderApprovalOutboxMessage(ApprovalOutboxEntity entity) {
        return OrderApprovalOutboxMessage.builder()
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
