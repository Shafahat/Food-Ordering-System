package com.food.ordering.system.order.service.messaging.kafka.listener;

import com.food.ordering.system.kafka.consumer.KafkaConsumerService;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResponseKafkaListener implements KafkaConsumerService<PaymentResponseAvroModel> {
    private final PaymentResponseMessageListener listener;
    private final OrderMessagingDataMapper mapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${order-service.payment-response-topic-name}")
    public void receive(@Payload List<PaymentResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of payment responses received with keys: {}, partitions:{} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(response -> {
            if (PaymentStatus.COMPLETED == response.getPaymentStatus()) {
                log.info("Processing successful payment for order id: {}", response.getOrderId());
                listener.paymentCompleted(mapper.mapToPaymentResponse(response));
            } else if (PaymentStatus.CANCELLED == response.getPaymentStatus() ||
                    PaymentStatus.FAILED == response.getPaymentStatus()) {
                log.info("Processing unsuccessful payment for order id: {}", response.getOrderId());
                listener.paymentCancelled(mapper.mapToPaymentResponse(response));
            }
        });
    }
}
