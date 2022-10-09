package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantDomainException;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.constants.DomainConstants.UTC;
import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxHelper {
    private final OrderOutboxRepository repository;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public Optional<OrderOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(UUID sagaId,
                                                                                              OutboxStatus status) {
        return repository.findByTypeAndSagaIdAndOutboxStatus(ORDER_SAGA_NAME, sagaId, status);
    }

    @Transactional(readOnly = true)
    public Optional<List<OrderOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus status) {
        return repository.findByTypeAndOutboxStatus(ORDER_SAGA_NAME, status);
    }

    @Transactional
    public void deleteOrderOutboxMessageByOutboxStatus(OutboxStatus status) {
        repository.deleteByTypeAndOutboxStatus(ORDER_SAGA_NAME, status);
    }

    @Transactional
    public void saveOrderOutboxMessage(OrderEventPayload payload, OrderApprovalStatus approvalStatus,
                                       OutboxStatus outboxStatus, UUID sagaId) {
        save(OrderOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(payload.getCreatedAt())
                .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .type(ORDER_SAGA_NAME)
                .payload(createPayload(payload))
                .approvalStatus(approvalStatus)
                .outboxStatus(outboxStatus)
                .build());
    }

    @Transactional
    public void updateOutboxStatus(OrderOutboxMessage orderPaymentOutboxMessage, OutboxStatus outboxStatus) {
        orderPaymentOutboxMessage.setOutboxStatus(outboxStatus);
        save(orderPaymentOutboxMessage);
        log.info("Order outbox table status is updated as: {}", outboxStatus.name());
    }

    private void save(OrderOutboxMessage orderPaymentOutboxMessage) {
        OrderOutboxMessage response = repository.save(orderPaymentOutboxMessage);
        if (response == null) {
            throw new RestaurantDomainException("Could not save OrderOutboxMessage!");
        }
        log.info("OrderOutboxMessage saved with id: {}", orderPaymentOutboxMessage.getId());
    }

    private String createPayload(OrderEventPayload payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderEventPayload json!", e);
            throw new RestaurantDomainException("Could not create OrderEventPayload json!", e);
        }
    }
}
