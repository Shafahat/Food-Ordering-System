package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurant_approval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.kafka.OrderKafkaMessageHelper;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {
    private final OrderMessagingDataMapper mapper;
    private final OrderServiceConfigData configData;
    private final OrderKafkaMessageHelper helper;
    private final KafkaProducerService<String, RestaurantApprovalRequestAvroModel> producer;

    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();

        try {
            RestaurantApprovalRequestAvroModel approvalRequest =
                    mapper.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);

            producer.send(configData.getRestaurantApprovalRequestTopicName(), orderId, approvalRequest,
                    helper.getKafkaCallBack(configData.getRestaurantApprovalRequestTopicName(), approvalRequest, orderId,
                            "RestaurantApprovalRequestAvroModel"));

            log.info("RestaurantApprovalRequestAvroModel sent to Kafka for order id: {}", approvalRequest.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalRequestAvroModel message" +
                    "to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }

    }
}
