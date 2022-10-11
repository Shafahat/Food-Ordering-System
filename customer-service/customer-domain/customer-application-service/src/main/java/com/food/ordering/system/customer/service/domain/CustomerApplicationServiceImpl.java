package com.food.ordering.system.customer.service.domain;

import com.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class CustomerApplicationServiceImpl implements CustomerApplicationService {
    private final CustomerCreateCommandHandler handler;
    private final CustomerDataMapper mapper;
    private final CustomerMessagePublisher publisher;

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerCommand command) {
        CustomerCreatedEvent customerCreatedEvent = handler.createCustomer(command);
        publisher.publish(customerCreatedEvent);
        return mapper.mapToCreateCustomerResponse(customerCreatedEvent.getCustomer(),
                "Customer saved successfully!");
    }
}
