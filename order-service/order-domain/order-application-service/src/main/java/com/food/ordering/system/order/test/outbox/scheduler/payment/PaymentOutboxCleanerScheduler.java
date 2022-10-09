package com.food.ordering.system.order.test.outbox.scheduler.payment;

import com.food.ordering.system.order.test.outbox.model.payment.OrderPaymentOutboxMessage;
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
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {
    private final PaymentOutboxHelper helper;

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> response = helper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.SUCCEEDED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED);


        if (response.isPresent()) {
            List<OrderPaymentOutboxMessage> messages = response.get();
            log.info("Received {} OrderPaymentOutboxMessage for clean-up. The Payloads :{}",
                    messages.size(),
                    messages.stream()
                            .map(OrderPaymentOutboxMessage::getPayload)
                            .collect(Collectors.joining(",")));
            helper.deletePaymentOutboxMessageByOutboxStatusAndSagaStatus(
                    OutboxStatus.COMPLETED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.FAILED,
                    SagaStatus.COMPENSATING);
            log.info("Clean-up completed ! DELETED LOG SIZE : {}", messages.size());
        }

    }
}
