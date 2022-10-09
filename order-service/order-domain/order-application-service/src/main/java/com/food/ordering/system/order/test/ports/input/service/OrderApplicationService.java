package com.food.ordering.system.order.test.ports.input.service;

import com.food.ordering.system.order.test.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.test.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.test.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.test.dto.track.TrackOrderResponse;

import javax.validation.Valid;

public interface OrderApplicationService {
    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
