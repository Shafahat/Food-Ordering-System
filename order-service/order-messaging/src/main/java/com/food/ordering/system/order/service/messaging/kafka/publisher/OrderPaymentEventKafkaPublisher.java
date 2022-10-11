package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.order.service.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {
    private final OrderServiceConfigData data;
    private final KafkaMessageHelper helper;
    private final OrderMessagingDataMapper mapper;
    private final KafkaProducerService<String, PaymentRequestAvroModel> producer;


    @Override
    public void publish(OrderPaymentOutboxMessage message,
                        BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> callback) {
        OrderPaymentEventPayload payload =
                helper.getOrderEventPayload(message.getPayload(), OrderPaymentEventPayload.class);

        String sagaId = message.getSagaId().toString();

        log.info("Received OrderPaymentOutboxMessage for order id: {} and saga id: {}", payload.getOrderId(), sagaId);

        try {
            PaymentRequestAvroModel request = mapper.mapToPaymentRequestAvroModel(sagaId, payload);

            producer.send(data.getPaymentRequestTopicName(), sagaId, request,
                    helper.getKafkaCallback(data.getPaymentRequestTopicName(), request, message, callback,
                            payload.getOrderId(), "PaymentRequestAvroModel"));

            log.info("OrderPaymentEventPayload sent to Kafka for order id: {} & saga id: {}", payload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload to kafka with" +
                    " order id: {} and saga id: {}, error: {}", payload.getOrderId(), sagaId, e.getMessage());
        }

    }
}
