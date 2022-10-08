package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.event.OrderPaidEvent;
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
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {
    private final OrderDomainService service;
    private final OrderSagaHelper sagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderDataMapper mapper;


    @Override
    @Transactional
    public void process(PaymentResponse response) {

        Optional<OrderPaymentOutboxMessage> optionalMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(response.getSagaId()),
                        SagaStatus.STARTED);

        if (optionalMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", response.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage message = optionalMessage.get();

        OrderPaidEvent event = completePaymentForOrder(response);

        SagaStatus sagaStatus = sagaHelper.orderStatusToSagaStatus(event.getOrder().getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(message, event.getOrder().getOrderStatus(), sagaStatus));

        approvalOutboxHelper.saveApprovalOutboxMessage(mapper.mapToOrderApprovalEventPayload(event),
                event.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(response.getSagaId()));

        log.info("Order with id: {} is paid", event.getOrder().getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse response) {

        Optional<OrderPaymentOutboxMessage> optionalMessage =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(response.getSagaId()),
                        getCurrentSagaStatus(response.getPaymentStatus()));

        if (optionalMessage.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!", response.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage message = optionalMessage.get();

        Order order = rollbackPaymentForOrder(response);

        SagaStatus sagaStatus = sagaHelper.orderStatusToSagaStatus(order.getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(message, order.getOrderStatus(), sagaStatus));

        if (response.getPaymentStatus() == PaymentStatus.CANCELLED) {
            approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(response.getSagaId(),
                    order.getOrderStatus(), sagaStatus));
        }

        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }

    private OrderPaidEvent completePaymentForOrder(PaymentResponse response) {
        log.info("Completing payment for order wth id: {}", response.getOrderId());
        Order order = sagaHelper.findOrder(response.getOrderId());
        OrderPaidEvent event = service.payOrder(order);
        sagaHelper.saveOrder(order);
        return event;
    }

    private Order rollbackPaymentForOrder(PaymentResponse response) {
        log.info("Cancelling order with id: {}", response.getOrderId());
        Order order = sagaHelper.findOrder(response.getOrderId());
        service.cancelOrder(order, response.getFailureMessages());
        sagaHelper.saveOrder(order);
        return order;
    }

    private SagaStatus[] getCurrentSagaStatus(PaymentStatus status) {
        return switch (status) {
            case COMPLETED -> new SagaStatus[]{SagaStatus.STARTED};
            case CANCELLED -> new SagaStatus[]{SagaStatus.PROCESSING};
            case FAILED -> new SagaStatus[]{SagaStatus.STARTED, SagaStatus.PROCESSING};
        };
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage message,
                                                                     OrderStatus orderStatus,
                                                                     SagaStatus sagaStatus) {
        message.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        message.setOrderStatus(orderStatus);
        message.setSagaStatus(sagaStatus);
        return message;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId,
                                                                       OrderStatus orderStatus,
                                                                       SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> optionalMessage =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId),
                        SagaStatus.COMPENSATING);

        if (optionalMessage.isEmpty()) {
            throw new OrderDomainException("Approval outbox message could not be found in " +
                    SagaStatus.COMPENSATING.name() + " status!");
        }

        OrderApprovalOutboxMessage message = optionalMessage.get();
        message.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        message.setOrderStatus(orderStatus);
        message.setSagaStatus(sagaStatus);
        return message;
    }
}
