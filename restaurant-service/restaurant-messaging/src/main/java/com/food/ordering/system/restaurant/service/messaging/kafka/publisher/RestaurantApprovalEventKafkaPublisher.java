package com.food.ordering.system.restaurant.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class RestaurantApprovalEventKafkaPublisher implements RestaurantApprovalResponseMessagePublisher {
    private final RestaurantMessagingDataMapper mapper;
    private final KafkaProducerService<String, RestaurantApprovalResponseAvroModel> producer;
    private final RestaurantServiceConfigData data;
    private final KafkaMessageHelper helper;

    @Override
    public void publish(OrderOutboxMessage message, BiConsumer<OrderOutboxMessage, OutboxStatus> callback) {
        OrderEventPayload payload = helper.getOrderEventPayload(message.getPayload(), OrderEventPayload.class);

        String sagaId = message.getSagaId().toString();

        log.info("Received OrderOutboxMessage for order id: {} and saga id: {}", payload.getOrderId(), sagaId);
        try {
            RestaurantApprovalResponseAvroModel response =
                    mapper.mapToRestaurantApprovalResponseAvroModel(sagaId, payload);

            producer.send(data.getRestaurantApprovalResponseTopicName(), sagaId, response,
                    helper.getKafkaCallback(data.getRestaurantApprovalResponseTopicName(), response, message, callback,
                            payload.getOrderId(), "RestaurantApprovalResponseAvroModel"));

            log.info("RestaurantApprovalResponseAvroModel sent to kafka for order id: {} and saga id: {}",
                    response.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message to kafka with " +
                    "order id: {} and saga id: {}, error: {}", payload.getOrderId(), sagaId, e.getMessage());
        }
    }

}
