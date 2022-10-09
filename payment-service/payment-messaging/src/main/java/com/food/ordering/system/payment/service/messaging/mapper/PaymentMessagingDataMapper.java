package com.food.ordering.system.payment.service.messaging.mapper;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
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

    public PaymentResponseAvroModel mapToPaymentResponseAvroModel(UUID sagaId, OrderEventPayload payload) {
        log.warn("{}", payload.toString());

        return PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(sagaId)
                .setPaymentId(UUID.fromString(payload.getPaymentId()))
                .setCustomerId(UUID.fromString(payload.getCustomerId()))
                .setOrderId(UUID.fromString(payload.getOrderId()))
                .setPrice(payload.getPrice())
                .setCreatedAt(payload.getCreatedAt().toInstant())
                .setPaymentStatus(PaymentStatus.valueOf(payload.getPaymentStatus()))
                .setFailureMessages(payload.getFailureMessages())
                .build();
    }
}
