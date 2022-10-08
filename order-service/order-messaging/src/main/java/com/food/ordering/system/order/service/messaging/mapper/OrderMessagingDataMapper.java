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
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.payment.service.domain.event.OrderPaidEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel mapToPaymentRequestAvroModel(OrderCreatedEvent event) {
        Order order = event.getOrder();

        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(order.getId().getValue())
                .setCustomerId(order.getCustomerId().getValue())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(event.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.valueOf(order.getOrderStatus().name()))
                .build();
    }

    public PaymentRequestAvroModel mapToPaymentRequestAvroModel(OrderCancelledEvent event) {
        Order order = event.getOrder();

        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(order.getId().getValue())
                .setCustomerId(order.getCustomerId().getValue())
                .setCreatedAt(event.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.valueOf(order.getOrderStatus().name()))
                .build();
    }

    public RestaurantApprovalRequestAvroModel mapToRestaurantApprovalRequestAvroModel(OrderPaidEvent event) {
        Order order = event.getOrder();

        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(order.getId().getValue())
                .setRestaurantId(order.getRestaurantId().getValue())
                .setProducts(order.getItems().stream()
                        .map(orderItem -> Product.newBuilder()
                                .setId(orderItem.getId().getValue().toString())
                                .setQuantity(orderItem.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(event.getCreatedAt().toInstant())
                .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(order.getOrderStatus().name()))
                .build();
    }

    public PaymentResponse mapToPaymentResponse(PaymentResponseAvroModel response) {
        return PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId("")
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
}
