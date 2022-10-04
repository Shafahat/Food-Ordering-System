package com.food.ordering.system.order.service.data.access.customer.adapter;

import com.food.ordering.system.order.service.data.access.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.order.service.data.access.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.payment.service.domain.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;
    private final CustomerDataAccessMapper mapper;

    @Override
    public Optional<Customer> findCustomer(UUID customerId) {
        return jpaRepository.findById(customerId).map(mapper::mapToCustomer);
    }
}
