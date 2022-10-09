package com.food.ordering.system.payment.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {
    public Payment mapToPayment(PaymentRequest request) {
        return Payment.builder()
                .customerId(new CustomerId(UUID.fromString(request.getCustomerId())))
                .orderId(new OrderId(UUID.fromString(request.getOrderId())))
                .price(new Money(request.getPrice()))
                .build();
    }

    public OrderEventPayload mapToOrderEventPayload(PaymentEvent event) {
        return OrderEventPayload.builder()
                .orderId(event.getPayment().getOrderId().getValue().toString())
                .customerId(event.getPayment().getCustomerId().getValue().toString())
                .price(event.getPayment().getPrice().getAmount())
                .paymentId(event.getPayment().getId().toString())
                .createdAt(event.getCreatedAt())
                .failureMessages(event.getFailureMessages())
                .paymentStatus(event.getPayment().getStatus().toString())
                .build();

    }
}
