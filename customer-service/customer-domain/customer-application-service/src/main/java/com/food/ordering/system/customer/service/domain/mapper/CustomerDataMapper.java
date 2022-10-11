package com.food.ordering.system.customer.service.domain.mapper;

import com.food.ordering.system.customer.service.domain.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataMapper {
    public Customer mapToCustomer(CreateCustomerCommand command) {
        return new Customer(new CustomerId(command.customerId()), command.username(), command.firstName(),
                command.lastName());
    }

    public CreateCustomerResponse mapToCreateCustomerResponse(Customer customer, String message) {
        return new CreateCustomerResponse(customer.getId().getValue(), message);
    }
}
