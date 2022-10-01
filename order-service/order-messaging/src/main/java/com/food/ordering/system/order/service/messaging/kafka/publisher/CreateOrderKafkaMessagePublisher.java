package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.kafka.OrderKafkaMessageHelper;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {
    private final OrderMessagingDataMapper mapper;
    private final OrderServiceConfigData configData;
    private final OrderKafkaMessageHelper helper;
    private final KafkaProducerService<String, PaymentRequestAvroModel> producer;

    @Override
    public void publish(OrderCreatedEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received OrderCreatedEvent for order id: {}", orderId);

        try {
            PaymentRequestAvroModel paymentRequest = mapper.orderCreatedEventToPaymentRequestAvroModel(domainEvent);

            producer.send(configData.getPaymentRequestTopicName(), orderId, paymentRequest,
                    helper.getKafkaCallBack(configData.getPaymentRequestTopicName(), paymentRequest, orderId,
                            "PaymentRequestAvroModel"));

            log.info("PaymentRequestAvroModel sent to Kafka for order id: {}", paymentRequest.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending PaymentRequestAvroModel message" +
                    "to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }
}
