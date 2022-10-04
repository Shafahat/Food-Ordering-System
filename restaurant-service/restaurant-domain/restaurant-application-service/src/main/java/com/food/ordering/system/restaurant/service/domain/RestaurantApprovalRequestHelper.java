package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalRequestHelper {
    private final RestaurantDomainService service;
    private final RestaurantDataMapper mapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
    private final OrderRejectedMessagePublisher orderRejectedMessagePublisher;

    public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest request) {
        log.info("Processing restaurant approval for order id: {}", request.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(request);
        OrderApprovalEvent orderApprovalEvent = service.validateOrder(restaurant, failureMessages);
        orderApprovalRepository.save(restaurant.getOrderApproval());
        return orderApprovalEvent;
    }


    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = mapper.mapToRestaurant(restaurantApprovalRequest);
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantInformation(restaurant);
        if (restaurantResult.isEmpty()) {
            log.error("Restaurant with id " + restaurant.getId().getValue() + " not found!");
            throw new RestaurantNotFoundException("Restaurant with id " + restaurant.getId().getValue() + "not found!");
        }
        Restaurant restaurantEntity = restaurantResult.get();
        restaurant.setActive(restaurantEntity.isActive());
        restaurant.getOrderDetail().getProducts().forEach(product -> restaurantEntity.getOrderDetail()
                .getProducts().forEach(p -> {
                    if (p.getId().equals(product.getId())) {
                        product.updateWithConfirmedNamePriceAndAvailablity(p.getName(), p.getPrice(), p.isAvailable());
                    }
                }));
        restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));

        return restaurant;
    }
}
