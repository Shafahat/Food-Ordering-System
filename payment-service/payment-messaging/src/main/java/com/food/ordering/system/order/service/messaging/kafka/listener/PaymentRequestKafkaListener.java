package com.food.ordering.system.order.service.messaging.kafka.listener;

import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.kafka.consumer.KafkaConsumerService;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.order.service.messaging.mapper.PaymentMessagingDataMapper;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestKafkaListener implements KafkaConsumerService<PaymentRequestAvroModel> {
    private final PaymentMessagingDataMapper mapper;
    private final PaymentRequestMessageListener listener;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${payment-service.payment-request-topic-name}")
    public void receive(@Payload List<PaymentRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment requests received with keys: {}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(request -> {
            if (Objects.equals(PaymentOrderStatus.PENDING.name(), request.getPaymentOrderStatus().name())) {
                log.info("Processing payment for order id: {}", request.getOrderId());
                listener.completePayment(mapper.mapToPaymentRequest(request));
            } else if (Objects.equals(PaymentOrderStatus.CANCELLED.name(), request.getPaymentOrderStatus().name())) {
                log.info("Cancelling payment for order id: {}", request.getOrderId());
                listener.cancelPayment(mapper.mapToPaymentRequest(request));
            }

        });
    }
}
