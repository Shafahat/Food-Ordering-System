package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.order.service.messaging.mapper.PaymentMessagingDataMapper;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentCancelledKafkaMessagePublisher implements PaymentCancelledMessagePublisher {
    private final PaymentMessagingDataMapper mapper;
    private final KafkaProducerService<String, PaymentResponseAvroModel> producer;
    private final PaymentServiceConfigData data;
    private final KafkaMessageHelper helper;

    @Override
    public void publish(PaymentCancelledEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

        log.info("Received PaymentCancelledEvent for order id: {}", orderId);

        try {
            PaymentResponseAvroModel response = mapper.mapToPaymentResponseAvroModel(domainEvent);

            producer.send(data.getPaymentResponseTopicName(), orderId, response,
                    helper.getKafkaCallBack(data.getPaymentResponseTopicName(), response, orderId,
                            "PaymentResponseAvroModel"));

            log.info("PaymentResponseAvroModel sent to Kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending PaymentResponseAvroModel message" +
                    "to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
