package com.food.ordering.system.order.test.ports.output.repository;

import com.food.ordering.system.payment.service.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
