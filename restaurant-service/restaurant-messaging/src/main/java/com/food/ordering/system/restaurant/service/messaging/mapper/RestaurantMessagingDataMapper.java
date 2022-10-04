package com.food.ordering.system.restaurant.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantMessagingDataMapper {

    public RestaurantApprovalRequest mapToRestaurantApproval(RestaurantApprovalRequestAvroModel request) {
        return RestaurantApprovalRequest.builder()
                .id(request.getId().toString())
                .sagaId(request.getSagaId().toString())
                .restaurantId(request.getRestaurantId().toString())
                .orderId(request.getOrderId().toString())
                .status(RestaurantOrderStatus.valueOf(request.getRestaurantOrderStatus().name()))
                .products(request.getProducts().stream().map(avroModel ->
                                Product.builder()
                                        .id(new ProductId(UUID.fromString(avroModel.getId())))
                                        .quantity(avroModel.getQuantity())
                                        .build())
                        .collect(Collectors.toList()))
                .price(request.getPrice())
                .createdAt(request.getCreatedAt())
                .build();
    }

    public RestaurantApprovalResponseAvroModel mapToRestaurantApprovalResponseAvroModel(OrderApprovedEvent event) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(event.getOrderApproval().getOrderId().getValue())
                .setRestaurantId(event.getOrderApproval().getRestaurantId().getValue())
                .setCreatedAt(event.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(event.getOrderApproval().getStatus().name()))
                .setFailureMessages(event.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponseAvroModel mapToRestaurantApprovalResponseAvroModel(OrderRejectedEvent event) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(event.getOrderApproval().getOrderId().getValue())
                .setRestaurantId(event.getOrderApproval().getRestaurantId().getValue())
                .setCreatedAt(event.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(event.getOrderApproval().getStatus().name()))
                .setFailureMessages(event.getFailureMessages())
                .build();
    }
}
