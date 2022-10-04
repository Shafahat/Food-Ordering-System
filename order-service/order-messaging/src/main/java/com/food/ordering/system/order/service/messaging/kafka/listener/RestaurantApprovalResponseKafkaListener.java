package com.food.ordering.system.order.service.messaging.kafka.listener;

import com.food.ordering.system.kafka.consumer.KafkaConsumerService;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurant_approval.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalResponseKafkaListener implements
        KafkaConsumerService<RestaurantApprovalResponseAvroModel> {

    private final RestaurantApprovalResponseMessageListener listener;
    private final OrderMessagingDataMapper mapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${order-service.restaurant-approval-response-topic-name}")
    public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of restaurant approval responses received with keys : {} , partitions : {} , offsets : {}",
                messages.size(), keys, partitions, offsets);

        messages.forEach(message -> {
            if (OrderApprovalStatus.APPROVED.equals(message.getOrderApprovalStatus())) {
                log.info("Processing successful restaurant approval response for order id: {}",
                        message.getOrderId().toString());
                listener.orderApproved(mapper.
                        mapToRestaurantApprovalResponse(message));
            } else if (OrderApprovalStatus.REJECTED.equals(message.getOrderApprovalStatus())) {
                log.info("Processing rejected restaurant approval response for order id: {}", message.getOrderId()
                        .toString());
                listener.orderRejected(mapper.mapToRestaurantApprovalResponse(message));
            }
        });
    }

}
