package com.food.ordering.system.order.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItemDto;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.entity.OrderItem;
import com.food.ordering.system.payment.service.domain.entity.Product;
import com.food.ordering.system.payment.service.domain.entity.Restaurant;
import com.food.ordering.system.payment.service.domain.valueobject.StreetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderDataMapper {

    public Restaurant mapToRestaurant(CreateOrderCommand createOrderCommand) {
        return Restaurant.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(createOrderCommand.getItems().stream()
                        .map(orderItemDto -> new Product(new ProductId(orderItemDto.getProductId())))
                        .toList())
                .build();
    }

    public Order mapToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(mapToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .items(mapToOrderItemEntities(createOrderCommand.getItems()))
                .build();
    }

    private List<OrderItem> mapToOrderItemEntities(
            List<OrderItemDto> items) {
        return items.stream()
                .map(orderItemDto ->
                        OrderItem.builder()
                                .product(new Product(new ProductId(orderItemDto.getProductId())))
                                .price(new Money(orderItemDto.getPrice()))
                                .quantity(orderItemDto.getQuantity())
                                .subTotal(new Money(orderItemDto.getSubTotal()))
                                .build())
                .collect(Collectors.toList());
    }

    private StreetAddress mapToStreetAddress(OrderAddress address) {
        return new StreetAddress(
                UUID.randomUUID(),
                address.getStreet(),
                address.getPostalCode(),
                address.getCity());
    }

    public CreateOrderResponse mapToCreateOrderResponse(Order order) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    public TrackOrderResponse mapToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages())
                .build();
    }

}
