package com.food.ordering.system.order.service.data.access.restaurant.adapter;

import com.food.ordering.system.common.data.access.entity.RestaurantEntity;
import com.food.ordering.system.common.data.access.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.data.access.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.payment.service.domain.entity.Restaurant;
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
