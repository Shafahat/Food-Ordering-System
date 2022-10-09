package com.food.ordering.system.restaurant.service.messaging.kafka.listener;

import com.food.ordering.system.kafka.consumer.KafkaConsumerService;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantApplicationServiceException;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestaurantApprovalRequestKafkaListener
        implements KafkaConsumerService<RestaurantApprovalRequestAvroModel> {
    private final RestaurantApprovalRequestMessageListener listener;
    private final RestaurantMessagingDataMapper mapper;

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(@Payload List<RestaurantApprovalRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of orders approval requests received with keys {}, partitions {} and offsets {}" +
                        ", sending for restaurant approval",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(request -> {
            try {
                log.info("Processing order approval for order id: {}", request.getOrderId());
                listener.approveOrder(mapper.mapToRestaurantApproval(request));
            } catch (DataAccessException e) {
                SQLException sqlException = (SQLException) e.getRootCause();
                if (sqlException != null && sqlException.getSQLState() != null &&
                        PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                    //NO-OP for unique constraint exception
                    log.error("Caught unique constraint exception with sql state: {} " +
                                    "in RestaurantApprovalRequestKafkaListener for order id: {}",
                            sqlException.getSQLState(), request.getOrderId());
                } else {
                    throw new RestaurantApplicationServiceException("Throwing DataAccessException in" +
                            " RestaurantApprovalRequestKafkaListener: " + e.getMessage(), e);
                }
            } catch (RestaurantNotFoundException e) {
                //NO-OP for RestaurantNotFoundException
                log.error("No payment found for order id: {}", request.getOrderId());
            }
        });
    }
}
