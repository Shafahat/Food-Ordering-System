package com.food.ordering.system.order.service.outbox.scheduler.payment;

import com.food.ordering.system.order.service.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
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
public class PaymentOutboxScheduler implements OutboxScheduler {
    private final PaymentOutboxHelper helper;
    private final PaymentRequestMessagePublisher publisher;

    @Override
    @Transactional
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> response = helper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.STARTED,
                SagaStatus.STARTED,
                SagaStatus.COMPENSATING);

        if (response.isPresent() && response.get().size() > 0) {
            List<OrderPaymentOutboxMessage> messages = response.get();
            log.info("Received {} OrderPaymentOutboxMessage with ids :  {} , sending message bus !",
                    messages.size(),
                    messages.stream()
                            .map(message -> message.getId().toString())
                            .collect(Collectors.joining(",")));
            messages.forEach(message -> publisher.publish(message, this::updateOutboxStatus));
            log.info("{} OrderPaymentOutboxMessage sent to message bus !", messages.size());
        }
    }

    private void updateOutboxStatus(OrderPaymentOutboxMessage message, OutboxStatus outboxStatus) {
        message.setOutboxStatus(outboxStatus);
        helper.save(message);
        log.info("Outbox message id : {} updated successfully with status: {}", message.getId(), outboxStatus.name());
    }
}