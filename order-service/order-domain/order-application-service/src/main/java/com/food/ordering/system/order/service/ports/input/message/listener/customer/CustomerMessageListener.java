package com.food.ordering.system.order.service.ports.input.message.listener.customer;

import com.food.ordering.system.order.service.dto.message.CustomerModel;

public interface CustomerMessageListener {
    void customerCreated(CustomerModel model);
}
