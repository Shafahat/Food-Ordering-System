package com.food.ordering.system.order.service.data.access.customer.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.data.access.customer.entity.CustomerEntity;
import com.food.ordering.system.payment.service.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public CustomerEntity customerToCustomerEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId().getValue())
                .build();
    }

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()));
    }

}
