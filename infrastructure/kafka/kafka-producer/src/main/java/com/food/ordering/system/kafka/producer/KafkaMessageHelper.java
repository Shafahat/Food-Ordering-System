package com.food.ordering.system.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessageHelper {
    private final ObjectMapper mapper;

    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String topic,
                                                                                   T request,
                                                                                   U message,
                                                                                   BiConsumer<U, OutboxStatus> callback,
                                                                                   String orderId,
                                                                                   String avroModelName) {
        return new ListenableFutureCallback<SendResult<String, T>>() {

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending {} with message: {} and outbox type: {} to topic {}",
                        avroModelName, request.toString(), message.getClass().getName(), topic, ex);
                callback.accept(message, OutboxStatus.FAILED);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Received successful response from Kafka for order id: {}" +
                                " Topic: {} Partition: {} Offset: {} Timestamp: {}",
                        orderId,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp());
                callback.accept(message, OutboxStatus.COMPLETED);
            }

        };
    }

    public <T> T getOrderEventPayload(String payload, Class<T> type) {
        try {
            return mapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Could not read " + type.getName() + " object!", e);
        }
    }
}
