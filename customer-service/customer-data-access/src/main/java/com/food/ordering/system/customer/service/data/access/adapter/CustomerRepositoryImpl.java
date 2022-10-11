package com.food.ordering.system.customer.service.data.access.adapter;

import com.food.ordering.system.customer.service.data.access.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.customer.service.data.access.repository.CustomerJpaRepository;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {
    private final CustomerJpaRepository repository;
    private final CustomerDataAccessMapper mapper;

    @Override
    public Customer createCustomer(Customer customer) {
        return mapper.mapToCustomer(repository.save(mapper.mapToCustomerEntity(customer)));
    }
}
