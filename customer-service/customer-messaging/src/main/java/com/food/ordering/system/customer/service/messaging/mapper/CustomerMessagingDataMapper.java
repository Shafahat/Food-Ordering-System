package com.food.ordering.system.customer.service.messaging.mapper;

import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.kafka.order.avro.model.CustomerAvroModel;
import org.springframework.stereotype.Component;

@Component
public class CustomerMessagingDataMapper {
    public CustomerAvroModel mapToPaymentResponse(CustomerCreatedEvent event) {
        return CustomerAvroModel.newBuilder()
                .setId(event.getCustomer().getId().getValue())
                .setUsername(event.getCustomer().getUsername())
                .setFirstName(event.getCustomer().getFirstName())
                .setLastName(event.getCustomer().getLastName())
                .build();
    }
}
