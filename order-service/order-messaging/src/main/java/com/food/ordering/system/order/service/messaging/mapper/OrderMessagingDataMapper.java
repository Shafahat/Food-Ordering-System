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
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel orderCreatedEventToPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent) {
        Order order = orderCreatedEvent.getOrder();

        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(order.getId().getValue())
                .setCustomerId(order.getCustomerId().getValue())
                .setCreatedAt(orderCreatedEvent.getCreateAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();
    }

    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelledEvent orderCancelledEvent) {
        Order order = orderCancelledEvent.getOrder();

        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setOrderId(order.getId().getValue())
                .setCustomerId(order.getCustomerId().getValue())
                .setCreatedAt(orderCancelledEvent.getCreateAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .build();
    }

    public RestaurantApprovalRequestAvroModel
    orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();

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
                .setCreatedAt(orderPaidEvent.getCreateAt().toInstant())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();
    }

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel message) {
        return PaymentResponse.builder()
                .id(UUID.randomUUID().toString())
                .sagaId("")
                .paymentId(message.getPaymentId().toString())
                .customerId(message.getCustomerId().toString())
                .orderId(message.getOrderId().toString())
                .price(message.getPrice())
                .createdAt(message.getCreatedAt())
                .paymentStatus(PaymentStatus.valueOf(message.getPaymentStatus().name()))
                .failureMessages(message.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse
    restaurantApprovalResponseAvroModelToRestaurantApprovalResponse(RestaurantApprovalResponseAvroModel message) {
        return RestaurantApprovalResponse.builder()
                .id(message.getId().toString())
                .orderId(message.getOrderId())
                .restaurantId(message.getRestaurantId().toString())
                .sagaId(message.getSagaId().toString())
                .createdAt(message.getCreatedAt())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(message.getOrderApprovalStatus().name()))
                .failureMessages(message.getFailureMessages())
                .build();
    }
}
