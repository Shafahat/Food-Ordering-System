package com.food.ordering.system.order.service.data.access.outbox.approval.adapter;

import com.food.ordering.system.order.service.data.access.outbox.approval.exception.ApprovalOutboxNotFoundException;
import com.food.ordering.system.order.service.data.access.outbox.approval.mapper.ApprovalOutboxDataAccessMapper;
import com.food.ordering.system.order.service.data.access.outbox.approval.repository.ApprovalOutboxJpaRepository;
import com.food.ordering.system.order.test.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.test.ports.output.repository.ApprovalOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApprovalOutboxRepositoryImpl implements ApprovalOutboxRepository {
    private final ApprovalOutboxJpaRepository repository;
    private final ApprovalOutboxDataAccessMapper mapper;

    @Override
    public OrderApprovalOutboxMessage save(OrderApprovalOutboxMessage message) {
        return mapper.mapToOrderApprovalOutboxMessage(repository.save(mapper.mapToOutboxEntity(message)));
    }

    @Override
    public Optional<List<OrderApprovalOutboxMessage>> findByTypeAndOutboxStatusAndSagaStatus(String sagaType,
                                                                                             OutboxStatus outboxStatus,
                                                                                             SagaStatus... sagaStatus) {
        return Optional.of(
                repository.findByTypeAndOutboxStatusAndSagaStatusIn(sagaType, outboxStatus, Arrays.asList(sagaStatus))
                        .orElseThrow(() -> new ApprovalOutboxNotFoundException("Approval outbox object " +
                                "could not be found for saga type " + sagaType))
                        .stream()
                        .map(mapper::mapToOrderApprovalOutboxMessage)
                        .collect(Collectors.toList()));
    }

    @Override
    public Optional<OrderApprovalOutboxMessage> findByTypeAndSagaIdAndSagaStatus(String type,
                                                                                 UUID sagaId,
                                                                                 SagaStatus... sagaStatus) {
        return repository.findByTypeAndSagaIdAndSagaStatusIn(type, sagaId, Arrays.asList(sagaStatus))
                .map(mapper::mapToOrderApprovalOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatusAndSagaStatus(String type,
                                                         OutboxStatus outboxStatus,
                                                         SagaStatus... sagaStatus) {
        repository.deleteByTypeAndOutboxStatusAndSagaStatusIn(type, outboxStatus, Arrays.asList(sagaStatus));
    }
}
