package com.food.ordering.system.restaurant.service.data.access.adapter;

import com.food.ordering.system.common.data.access.entity.RestaurantEntity;
import com.food.ordering.system.common.data.access.repository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.data.access.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {
    private final RestaurantJpaRepository jpaRepository;
    private final RestaurantDataAccessMapper mapper;

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> restaurantProducts = mapper.mapToRestaurantProducts(restaurant);
        Optional<List<RestaurantEntity>> restaurantEntities = jpaRepository
                .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), restaurantProducts);
        return restaurantEntities.map(mapper::mapToRestaurant);
    }
}