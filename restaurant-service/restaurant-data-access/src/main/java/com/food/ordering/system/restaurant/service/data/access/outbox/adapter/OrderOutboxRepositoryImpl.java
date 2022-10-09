package com.food.ordering.system.restaurant.service.data.access.outbox.adapter;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.data.access.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.restaurant.service.data.access.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.restaurant.service.data.access.outbox.repository.OrderOutboxJpaRepository;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {
    private final OrderOutboxJpaRepository repository;
    private final OrderOutboxDataAccessMapper mapper;

    @Override
    public OrderOutboxMessage save(OrderOutboxMessage orderPaymentOutboxMessage) {
        return mapper.mapToOrderOutboxMessage(repository.save(mapper.mapToOutboxEntity(orderPaymentOutboxMessage)));
    }

    @Override
    public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus status) {
        return Optional.of(repository.findByTypeAndOutboxStatus(type, status)
                .orElseThrow(() -> new OrderOutboxNotFoundException("Approval outbox object " +
                        "cannot be found for saga type " + type))
                .stream()
                .map(mapper::mapToOrderOutboxMessage)
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId,
                                                                           OutboxStatus status) {
        return repository.findByTypeAndSagaIdAndOutboxStatus(type, sagaId, status)
                .map(mapper::mapToOrderOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus status) {
        repository.deleteByTypeAndOutboxStatus(type, status);
    }
}
