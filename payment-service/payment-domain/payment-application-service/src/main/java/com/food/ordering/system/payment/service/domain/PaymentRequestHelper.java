package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;


    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received payment complete event for id : {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.mapToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistory = getCreditHistory(payment.getCustomerId());
        List<String> failureMessage = new ArrayList<>();

        PaymentEvent paymentEvent = paymentDomainService.validateAndInitializePayment
                (payment, creditEntry, creditHistory, failureMessage);
        paymentRepository.save(payment);

        persistDbObject(payment, creditEntry, creditHistory, failureMessage);
        return paymentEvent;
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        log.info("Received payment cancel event for id : {}", paymentRequest.getOrderId());
        Payment payment = getPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistory = getCreditHistory(payment.getCustomerId());
        List<String> failureMessage = new ArrayList<>();

        PaymentEvent paymentEvent = paymentDomainService.validateAndCancelledPayment
                (payment, creditEntry, creditHistory, failureMessage);

        persistDbObject(payment, creditEntry, creditHistory, failureMessage);
        return paymentEvent;
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

    private Payment getPayment(PaymentRequest paymentRequest) {
        return paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId())).orElseThrow(
                () -> new PaymentNotFoundException("Payment not found"));
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        return creditHistoryRepository.findByCustomerId(customerId).orElseThrow(
                () -> new PaymentApplicationServiceException
                        ("No credit history found for customer id : " + customerId));
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        return creditEntryRepository.findByCustomerId(customerId).orElseThrow(
                () -> new PaymentApplicationServiceException
                        ("Credit entry not found for customer id : " + customerId));
    }
}
