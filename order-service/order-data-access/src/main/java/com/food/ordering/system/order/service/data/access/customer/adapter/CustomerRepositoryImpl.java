package com.food.ordering.system.order.service.data.access.customer.adapter;

import com.food.ordering.system.order.service.data.access.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.order.service.data.access.customer.repository.CustomerJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository repository;
    private final CustomerDataAccessMapper mapper;

    @Override
    public Optional<Customer> findCustomer(UUID customerId) {
        return repository.findById(customerId).map(mapper::mapToCustomer);
    }

    @Override
    public Customer save(Customer customer) {
        return mapper.mapToCustomer(repository.save(mapper.mapToCustomerEntity(customer)));
    }

}
