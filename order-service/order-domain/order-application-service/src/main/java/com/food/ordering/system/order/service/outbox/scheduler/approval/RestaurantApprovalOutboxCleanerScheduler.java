package com.food.ordering.system.order.service.outbox.scheduler.approval;

import com.food.ordering.system.order.service.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalOutboxCleanerScheduler implements OutboxScheduler {
    private final ApprovalOutboxHelper helper;

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> response = helper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);

        if (response.isPresent()) {
            List<OrderApprovalOutboxMessage> messages = response.get();
            log.info("Received {} OrderApprovalOutboxMessage for clean-up. The Payloads :{}",
                    messages.size(),
                    messages.stream()
                            .map(OrderApprovalOutboxMessage::getPayload)
                            .collect(Collectors.joining(",")));
            helper.getApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                    OutboxStatus.COMPLETED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.FAILED,
                    SagaStatus.COMPENSATING);
            log.info("Clean-up completed ! DELETED LOG SIZE : {}", messages.size());
        }

    }
}
