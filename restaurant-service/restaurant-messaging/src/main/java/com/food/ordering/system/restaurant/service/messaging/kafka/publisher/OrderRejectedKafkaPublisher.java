package com.food.ordering.system.restaurant.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderRejectedKafkaPublisher implements OrderRejectedMessagePublisher {
    private final RestaurantMessagingDataMapper mapper;
    private final KafkaProducerService<String, RestaurantApprovalResponseAvroModel> producer;
    private final RestaurantServiceConfigData data;
    private final KafkaMessageHelper helper;

    @Override
    public void publish(OrderRejectedEvent domainEvent) {
        String orderId = domainEvent.getOrderApproval().getOrderId().getValue().toString();
        log.info("Received OrderRejectedEvent for order id: {}", orderId);

        try {
            RestaurantApprovalResponseAvroModel response = mapper.mapToRestaurantApprovalResponseAvroModel(domainEvent);

            producer.send(data.getRestaurantApprovalResponseTopicName(), orderId, response,
                    helper.getKafkaCallBack(data.getRestaurantApprovalResponseTopicName(), response, orderId,
                            "RestaurantApprovalResponseAvroModel"));

            log.info("RestaurantApprovalResponseAvroModel sent to Kafka at: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message" +
                    "to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}

