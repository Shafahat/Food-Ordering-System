package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurant_approval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {
    private final OrderServiceConfigData data;
    private final KafkaMessageHelper helper;
    private final OrderMessagingDataMapper mapper;
    private final KafkaProducerService<String, RestaurantApprovalRequestAvroModel> producer;


    @Override
    public void publish(OrderApprovalOutboxMessage message,
                        BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> callback) {
        OrderApprovalEventPayload payload =
                helper.getOrderEventPayload(message.getPayload(), OrderApprovalEventPayload.class);

        String sagaId = message.getSagaId().toString();

        log.info("Received OrderApprovalOutboxMessage for order id: {} and saga id: {}", payload.getOrderId(), sagaId);

        try {
            RestaurantApprovalRequestAvroModel request = mapper.mapToRestaurantApprovalRequestAvroModel(sagaId, payload);

            producer.send(data.getRestaurantApprovalRequestTopicName(), sagaId, request,
                    helper.getKafkaCallback(data.getRestaurantApprovalRequestTopicName(), request, message, callback,
                            payload.getOrderId(), "ApprovalRequestAvroModel"));

            log.info("OrderApprovalEventPayload sent to Kafka for order id: {} & saga id: {}",
                    payload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderApprovalEventPayload to kafka with" +
                    " order id: {} and saga id: {}, error: {}", payload.getOrderId(), sagaId, e.getMessage());
        }

    }


}
