package com.food.ordering.system.restaurant.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantDataMapper {

    public Restaurant mapToRestaurant(RestaurantApprovalRequest request) {
        return Restaurant.builder()
                .id(new RestaurantId(UUID.fromString(request.getRestaurantId())))
                .orderDetail(OrderDetail.builder()
                        .id(new OrderId(UUID.fromString(request.getOrderId())))
                        .products(request.getProducts().stream().map(
                                product -> Product.builder()
                                        .id(product.getId())
                                        .quantity(product.getQuantity())
                                        .build()
                        ).toList())
                        .totalAmount(new Money(request.getPrice()))
                        .status(OrderStatus.valueOf(request.getStatus().name()))
                        .build())
                .build();
    }
//    public OrderEventPayload
//    orderApprovalEventToOrderEventPayload(OrderApprovalEvent orderApprovalEvent) {
//        return OrderEventPayload.builder()
//                .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
//                .restaurantId(orderApprovalEvent.getRestaurantId().getValue().toString())
//                .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getStatus().name())
//                .createdAt(orderApprovalEvent.getCreatedAt())
//                .failureMessages(orderApprovalEvent.getFailureMessages())
//                .build();
//    }
}
