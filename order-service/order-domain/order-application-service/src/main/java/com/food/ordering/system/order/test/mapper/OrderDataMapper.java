package com.food.ordering.system.order.test.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.order.test.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.test.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.test.dto.create.OrderAddress;
import com.food.ordering.system.order.test.dto.create.OrderItemDto;
import com.food.ordering.system.order.test.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.test.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.test.outbox.model.approval.OrderApprovalEventProduct;
import com.food.ordering.system.order.test.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.entity.OrderItem;
import com.food.ordering.system.payment.service.domain.entity.Product;
import com.food.ordering.system.payment.service.domain.entity.Restaurant;
import com.food.ordering.system.payment.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.payment.service.domain.event.OrderPaidEvent;
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

    public OrderPaymentEventPayload mapToOrderPaymentEventPayload(OrderCreatedEvent event) {
        return OrderPaymentEventPayload.builder()
                .orderId(event.getOrder().getId().getValue().toString())
                .customerId(event.getOrder().getCustomerId().getValue().toString())
                .price(event.getOrder().getPrice().getAmount())
                .createdAt(event.getCreatedAt())
                .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
                .build();
    }

    public OrderApprovalEventPayload mapToOrderApprovalEventPayload(OrderPaidEvent event) {
        return OrderApprovalEventPayload.builder()
                .orderId(event.getOrder().getId().getValue().toString())
                .restaurantId(event.getOrder().getRestaurantId().getValue().toString())
                .restaurantOrderStatus(RestaurantOrderStatus.PAID.name())
                .products(event.getOrder().getItems().stream().map(orderItem ->
                        OrderApprovalEventProduct.builder()
                                .id(orderItem.getProduct().getId().getValue().toString())
                                .quantity(orderItem.getQuantity())
                                .build()).toList())
                .price(event.getOrder().getPrice().getAmount())
                .createdAt(event.getCreatedAt())
                .build();
    }

    public OrderPaymentEventPayload mapToOrderPaymentEventPayload(OrderCancelledEvent event) {
        return OrderPaymentEventPayload.builder()
                .orderId(event.getOrder().getId().getValue().toString())
                .paymentOrderStatus(PaymentOrderStatus.CANCELLED.name())
                .customerId(event.getOrder().getCustomerId().getValue().toString())
                .price(event.getOrder().getPrice().getAmount())
                .createdAt(event.getCreatedAt())
                .build();
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
