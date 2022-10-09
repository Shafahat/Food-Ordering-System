package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.Product;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantOrderStatus;
import com.food.ordering.system.order.test.dto.message.PaymentResponse;
import com.food.ordering.system.order.test.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.test.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.test.outbox.model.payment.OrderPaymentEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderMessagingDataMapper {

    public PaymentResponse mapToPaymentResponse(PaymentResponseAvroModel response) {
        return PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId(response.getSagaId().toString())
                .paymentId(response.getPaymentId().toString())
                .customerId(response.getCustomerId().toString())
                .orderId(response.getOrderId().toString())
                .price(response.getPrice())
                .createdAt(response.getCreatedAt())
                .paymentStatus(PaymentStatus.valueOf(response.getPaymentStatus().name()))
                .failureMessages(response.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse mapToRestaurantApprovalResponse(RestaurantApprovalResponseAvroModel response) {
        return RestaurantApprovalResponse.builder()
                .id(response.getId().toString())
                .orderId(response.getOrderId().toString())
                .restaurantId(response.getRestaurantId().toString())
                .sagaId(response.getSagaId().toString())
                .createdAt(response.getCreatedAt())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(response.getOrderApprovalStatus().name()))
                .failureMessages(response.getFailureMessages())
                .build();
    }

    public PaymentRequestAvroModel mapToPaymentRequestAvroModel(String sagaId, OrderPaymentEventPayload payload) {
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.fromString(sagaId))
                .setCustomerId(UUID.fromString(payload.getCustomerId()))
                .setOrderId(UUID.fromString(payload.getOrderId()))
                .setPrice(payload.getPrice())
                .setCreatedAt(payload.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.valueOf(payload.getPaymentOrderStatus()))
                .build();
    }

    public RestaurantApprovalRequestAvroModel mapToRestaurantApprovalRequestAvroModel(String sagaId,
                                                                                      OrderApprovalEventPayload payload) {
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.fromString(sagaId))
                .setOrderId(UUID.fromString(payload.getOrderId()))
                .setRestaurantId(UUID.fromString(payload.getRestaurantId()))
                .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(payload.getRestaurantOrderStatus()))
                .setProducts(payload.getProducts().stream().map(orderApprovalEventProduct -> Product.newBuilder()
                        .setId(orderApprovalEventProduct.getId())
                        .setQuantity(orderApprovalEventProduct.getQuantity())
                        .build()).toList())
                .setPrice(payload.getPrice())
                .setCreatedAt(payload.getCreatedAt().toInstant())
                .build();
    }
}
