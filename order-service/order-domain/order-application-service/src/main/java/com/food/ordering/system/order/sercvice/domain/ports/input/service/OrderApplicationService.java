package com.food.ordering.system.order.sercvice.domain.ports.input.service;

import com.food.ordering.system.order.sercvice.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.sercvice.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.sercvice.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.sercvice.domain.dto.track.TrackOrderResponse;

import javax.validation.Valid;

public interface OrderApplicationService {
    CreateOrderResponse createOrder(@Valid CreateOrderCommand createOrderCommand);

    TrackOrderResponse trackOrder(@Valid TrackOrderQuery trackOrderQuery);
}
