package com.food.ordering.system.payment.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventKafkaPublisher implements PaymentResponseMessagePublisher {
    private final PaymentMessagingDataMapper mapper;
    private final KafkaProducerService<String, PaymentResponseAvroModel> producer;
    private final PaymentServiceConfigData data;
    private final KafkaMessageHelper helper;

    @Override
    public void publish(OrderOutboxMessage message, BiConsumer<OrderOutboxMessage, OutboxStatus> callback) {
        OrderEventPayload payload = helper.getOrderEventPayload(message.getPayload(), OrderEventPayload.class);



        log.info("Publishing payment response for order id: {}",  message.getSagaId());

        try {
            PaymentResponseAvroModel response = mapper.mapToPaymentResponseAvroModel(message.getSagaId(), payload);

            producer.send(data.getPaymentResponseTopicName(), message.getSagaId().toString(), response,
                    helper.getKafkaCallback(data.getPaymentResponseTopicName(), response, message,
                            callback, payload.getOrderId(), "PaymentResponseAvroModel"));

            log.info("PaymentResponseAvroModel sent to kafka for order id: {} and saga id: {}",
                    response.getOrderId(), message.getSagaId());
        } catch (Exception e) {
            log.error("Error while publishing payment response for order id: {}", message.getSagaId(), e);
        }
    }
}

