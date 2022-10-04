package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.order.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.order.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.order.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {

    public PaymentRequest mapToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getId().toString())
                .sagaId(paymentRequestAvroModel.getSagaId().toString())
                .customerId(paymentRequestAvroModel.getCustomerId().toString())
                .orderId(paymentRequestAvroModel.getOrderId().toString())
                .price(paymentRequestAvroModel.getPrice())
                .createdAt(paymentRequestAvroModel.getCreatedAt())
                .status(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
                .build();
    }

    public PaymentResponseAvroModel mapToPaymentResponseAvroModel(PaymentCompletedEvent event) {
        return getPaymentResponseAvroModel(event.getPayment(), event.getFailureMessages());
    }

    public PaymentResponseAvroModel mapToPaymentResponseAvroModel(PaymentCancelledEvent event) {
        return getPaymentResponseAvroModel(event.getPayment(), event.getFailureMessages());
    }

    public PaymentResponseAvroModel mapToPaymentResponseAvroModel(PaymentFailedEvent event) {
        return getPaymentResponseAvroModel(event.getPayment(), event.getFailureMessages());
    }

    private PaymentResponseAvroModel getPaymentResponseAvroModel(Payment payment, List<String> failureMessages) {
        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.randomUUID())
                .setPaymentId(payment.getId().getValue())
                .setCustomerId(payment.getCustomerId().getValue())
                .setOrderId(payment.getOrderId().getValue())
                .setPrice(payment.getPrice().getAmount())
                .setCreatedAt(payment.getCreatedAt().toInstant())
                .setPaymentStatus(PaymentStatus.valueOf(payment.getStatus().name()))
                .setFailureMessages(failureMessages)
                .build();
    }
}
