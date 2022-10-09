package com.food.ordering.system.restaurant.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
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
                .products(request.getProducts().stream()
                        .map(avroModel -> Product.builder()
                                .id(new ProductId(UUID.fromString(avroModel.getId())))
                                .quantity(avroModel.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .price(request.getPrice())
                .createdAt(request.getCreatedAt())
                .build();
    }

    public RestaurantApprovalResponseAvroModel mapToRestaurantApprovalResponseAvroModel(String sagaId,
                                                                                        OrderEventPayload payload) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.fromString(sagaId))
                .setOrderId(UUID.fromString(payload.getOrderId()))
                .setRestaurantId(UUID.fromString(payload.getRestaurantId()))
                .setCreatedAt(payload.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(payload.getOrderApprovalStatus()))
                .setFailureMessages(payload.getFailureMessages())
                .build();
    }
}
