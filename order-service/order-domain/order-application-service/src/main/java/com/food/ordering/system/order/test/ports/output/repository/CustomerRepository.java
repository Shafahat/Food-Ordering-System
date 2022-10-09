package com.food.ordering.system.order.test.ports.output.repository;

import com.food.ordering.system.payment.service.domain.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Optional<Customer> findCustomer(UUID customerId);
}
