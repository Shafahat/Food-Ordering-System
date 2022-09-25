package com.food.ordering.system.order.sercvice.domain.ports.output.repository;

import com.food.ordering.system.order.sercvice.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
