package com.food.ordering.system.customer.service.data.access.mapper;

import com.food.ordering.system.customer.service.data.access.entity.CustomerEntity;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {
    public Customer mapToCustomer(CustomerEntity entity) {
        return new Customer(new CustomerId(entity.getId()),
                entity.getUsername(),
                entity.getFirstName(),
                entity.getLastName());
    }

    public CustomerEntity mapToCustomerEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId().getValue())
                .username(customer.getUsername())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .build();
    }

}
