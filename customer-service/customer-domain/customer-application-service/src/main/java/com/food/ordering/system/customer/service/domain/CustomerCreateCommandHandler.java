package com.food.ordering.system.customer.service.domain;

import com.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
class CustomerCreateCommandHandler {
    private final CustomerDomainService service;
    private final CustomerRepository repository;
    private final CustomerDataMapper mapper;

    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand command) {
        Customer customer = mapper.mapToCustomer(command);
        CustomerCreatedEvent event = service.validateAndInitiateCustomer(customer);
        Customer savedCustomer = repository.createCustomer(customer);
        if (savedCustomer == null) {
            log.error("Could not save customer with id: {}", command.customerId());
            throw new CustomerDomainException("Could not save customer with id " + command.customerId());
        }
        log.info("Returning CustomerCreatedEvent for customer id: {}", command.customerId());
        return event;
    }
}
