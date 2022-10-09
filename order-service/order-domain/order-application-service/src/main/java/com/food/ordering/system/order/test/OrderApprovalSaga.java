package com.food.ordering.system.order.test;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.order.test.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.test.mapper.OrderDataMapper;
import com.food.ordering.system.order.test.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.test.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.test.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.test.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.payment.service.domain.exception.OrderDomainException;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.constants.DomainConstants.UTC;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {
    private final OrderDomainService service;
    private final OrderSagaHelper sagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper mapper;

    @Override
    @Transactional
    public void process(RestaurantApprovalResponse response) {
        Optional<OrderApprovalOutboxMessage> optionalMessage = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(response.getSagaId()),
                SagaStatus.PROCESSING);

        if (optionalMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", response.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage message = optionalMessage.get();

        Order order = approveOrder(response);

        SagaStatus sagaStatus = sagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(message,
                order.getOrderStatus(), sagaStatus));

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(response.getSagaId(),
                order.getOrderStatus(), sagaStatus));

        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(RestaurantApprovalResponse response) {
        Optional<OrderApprovalOutboxMessage> optionalMessage = approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(response.getSagaId()),
                SagaStatus.PROCESSING);

        if (optionalMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!", response.getSagaId());
            return;
        }

        OrderApprovalOutboxMessage message = optionalMessage.get();

        OrderCancelledEvent event = rollbackOrder(response);

        SagaStatus sagaStatus = sagaHelper.orderStatusToSagaStatus(event.getOrder().getOrderStatus());

        approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(message,
                event.getOrder().getOrderStatus(), sagaStatus));

        paymentOutboxHelper.savePaymentOutboxMessage(mapper.mapToOrderPaymentEventPayload(event),
                event.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(response.getSagaId()));

        log.info("Order with id: {} is cancelling", event.getOrder().getId().getValue());
    }

    private OrderCancelledEvent rollbackOrder(RestaurantApprovalResponse response) {
        log.info("Cancelling order with id: {}", response.getOrderId());
        Order order = sagaHelper.findOrder(response.getOrderId());
        OrderCancelledEvent event = service.cancelOrderPayment(order, response.getFailureMessages());
        sagaHelper.saveOrder(order);
        return event;
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(String sagaId,
                                                                     OrderStatus orderStatus,
                                                                     SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> optionalMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId),
                        SagaStatus.PROCESSING);

        if (optionalMessage.isEmpty()) {
            throw new OrderDomainException("Payment outbox message could not be found in " +
                    SagaStatus.PROCESSING.name() + " status!");
        }

        OrderPaymentOutboxMessage message = optionalMessage.get();
        message.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        message.setOrderStatus(orderStatus);
        message.setSagaStatus(sagaStatus);
        return message;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(OrderApprovalOutboxMessage message,
                                                                       OrderStatus orderStatus,
                                                                       SagaStatus sagaStatus) {
        message.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        message.setOrderStatus(orderStatus);
        message.setSagaStatus(sagaStatus);
        return message;
    }

    private Order approveOrder(RestaurantApprovalResponse response) {
        log.info("Approving order with id: {}", response.getOrderId());
        Order order = sagaHelper.findOrder(response.getOrderId());
        service.approveOrder(order);
        sagaHelper.saveOrder(order);
        return order;
    }

}
