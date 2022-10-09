package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestHelper {
    private final PaymentDomainService service;
    private final OrderOutboxHelper helper;
    private final PaymentResponseMessagePublisher publisher;
    private final PaymentDataMapper mapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;


    @Transactional
    public void persistPayment(PaymentRequest request) {

        if (publishIfOutboxMessageProcessedForPayment(request, PaymentStatus.COMPLETED)) {
            log.info("Outbox Message with sagaId : {} already save !", request.getSagaId());
            return;
        }

        log.info("Received payment complete event for id : {}", request.getOrderId());
        Payment payment = mapper.mapToPayment(request);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistory = getCreditHistory(payment.getCustomerId());
        List<String> failureMessage = new ArrayList<>();

        PaymentEvent event = service.validateAndInitializePayment(payment, creditEntry, creditHistory, failureMessage);

        persistDbObject(payment, creditEntry, creditHistory, failureMessage);

        helper.saveOrderOutboxMessage(mapper.mapToOrderEventPayload(event), event.getPayment().getStatus(),
                OutboxStatus.STARTED, UUID.fromString(request.getSagaId()));
    }

    private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest request, PaymentStatus paymentStatus) {
        Optional<OrderOutboxMessage> outboxMessage = helper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(
                UUID.fromString(request.getSagaId()), paymentStatus);

        if (outboxMessage.isPresent()) {
            publisher.publish(outboxMessage.get(), helper::updateOutboxMessage);
            return true;
        }
        return false;
    }

    public void persistCancelPayment(PaymentRequest request) {

        if (publishIfOutboxMessageProcessedForPayment(request, PaymentStatus.CANCELLED)) {
            log.info("Outbox Message with sagaId : {} already save !", request.getSagaId());
            return;
        }

        log.info("Received payment cancel event for id : {}", request.getOrderId());
        Payment payment = paymentRepository.findByOrderId(UUID.fromString(request.getOrderId()))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistory = getCreditHistory(payment.getCustomerId());
        List<String> failureMessage = new ArrayList<>();
        PaymentEvent event = service.validateAndCancelledPayment(payment, creditEntry, creditHistory, failureMessage);

        persistDbObject(payment, creditEntry, creditHistory, failureMessage);

        helper.saveOrderOutboxMessage(mapper.mapToOrderEventPayload(event), event.getPayment().getStatus(),
                OutboxStatus.STARTED, UUID.fromString(request.getSagaId()));
    }

    private void persistDbObject(Payment payment,
                                 CreditEntry creditEntry,
                                 List<CreditHistory> creditHistory,
                                 List<String> failureMessage) {
        paymentRepository.save(payment);
        if (failureMessage.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistory.get(creditHistory.size() - 1));
        }
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        return creditHistoryRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new PaymentApplicationServiceException("No credit history found for customer id : "
                        + customerId));
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        return creditEntryRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new PaymentApplicationServiceException("Credit entry not found for customer id : "
                        + customerId));
    }
}
