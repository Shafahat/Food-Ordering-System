package com.food.ordering.system.order.service.messaging.kafka.listener;

import com.food.ordering.system.kafka.consumer.KafkaConsumerService;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.order.service.ports.input.message.listener.customer.CustomerMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerKafkaListener implements KafkaConsumerService<CustomerAvroModel> {
    private final CustomerMessageListener listener;
    private final OrderMessagingDataMapper mapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.customer-group-id}",
            topics = "${order-service.customer-topic-name}")
    public void receive(List<CustomerAvroModel> messages,
                        List<String> keys,
                        List<Integer> partitions,
                        List<Long> offSets) {
        log.info("{} number of customer create messages received with keys {}, partitions {} and offsets {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offSets.toString());

        messages.forEach(customerAvroModel -> listener.customerCreated(mapper.mapToCustomerModel(customerAvroModel)));
    }
}
