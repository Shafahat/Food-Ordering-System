package com.food.ordering.system.order.service.messaging.kafka.publisher;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducerService;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.kafka.OrderKafkaMessageHelper;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelOrderKafkaMessagePublisher implements OrderCancelledPaymentRequestMessagePublisher {
    private final OrderMessagingDataMapper mapper;
    private final OrderServiceConfigData configData;
    private final OrderKafkaMessageHelper helper;
    private final KafkaProducerService<String, PaymentRequestAvroModel> producer;

    @Override
    public void publish(OrderCancelledEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Received OrderCancelledEvent for order id: {}", orderId);

        try {
            PaymentRequestAvroModel paymentRequest = mapper.orderCancelledEventToPaymentRequestAvroModel(domainEvent);

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
