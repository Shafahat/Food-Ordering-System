package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxScheduler implements OutboxScheduler {
    private final OrderOutboxHelper helper;
    private final RestaurantApprovalResponseMessagePublisher publisher;

    @Transactional
    @Scheduled(fixedRateString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}")
    @Override
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> response =
                helper.getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
        if (response.isPresent() && !response.get().isEmpty()) {
            List<OrderOutboxMessage> message = response.get();
            log.info("Received {} OrderOutboxMessage with ids {}, sending to message bus!", message.size(),
                    message.stream().map(outboxMessage -> outboxMessage.getId().toString())
                            .collect(Collectors.joining(",")));
            message.forEach(orderOutboxMessage -> publisher.publish(orderOutboxMessage, helper::updateOutboxStatus));
            log.info("{} OrderOutboxMessage sent to message bus!", message.size());
        }
    }

}
