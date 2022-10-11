package com.food.ordering.system.order.service;

import com.food.ordering.system.order.service.dto.message.CustomerModel;
import com.food.ordering.system.order.service.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.ports.input.message.listener.customer.CustomerMessageListener;
import com.food.ordering.system.order.service.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerMessageListenerImpl implements CustomerMessageListener {
    private final CustomerRepository repository;
    private final OrderDataMapper mapper;

    @Override
    public void customerCreated(CustomerModel model) {
        var customer = repository.save
                (mapper.mapToCustomer(model));

        if (Objects.isNull(customer)) {
            log.error("Customer not created");
        } else {
            log.info("Customer created");
        }
    }
}