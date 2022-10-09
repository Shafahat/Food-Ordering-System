package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.RestaurantApprovalResponseMessagePublisher;
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
    private final OrderOutboxHelper helper;
    private final RestaurantDataMapper mapper;
    private final RestaurantDomainService service;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final RestaurantApprovalResponseMessagePublisher publisher;

    public void persistOrderApproval(RestaurantApprovalRequest request) {
        if (publishIfOutboxMessageProcessed(request)) {
            log.info("An outbox message with saga id: {} already saved to database!",
                    request.getSagaId());
            return;
        }
        log.info("Processing restaurant approval for order id: {}", request.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(request);
        OrderApprovalEvent orderApprovalEvent = service.validateOrder(restaurant, failureMessages);
        orderApprovalRepository.save(restaurant.getOrderApproval());

        helper.saveOrderOutboxMessage(mapper.mapToOrderEventPayload(orderApprovalEvent),
                orderApprovalEvent.getOrderApproval().getStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(request.getSagaId()));
    }


    private Restaurant findRestaurant(RestaurantApprovalRequest request) {
        Restaurant restaurant = mapper.mapToRestaurant(request);

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
        restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(request.getOrderId())));

        return restaurant;
    }

    private boolean publishIfOutboxMessageProcessed(RestaurantApprovalRequest request) {
        Optional<OrderOutboxMessage> message = helper.getCompletedOrderOutboxMessageBySagaIdAndOutboxStatus(
                UUID.fromString(request.getSagaId()), OutboxStatus.COMPLETED);
        if (message.isPresent()) {
            publisher.publish(message.get(), helper::updateOutboxStatus);
            return true;
        }
        return false;
    }
}
