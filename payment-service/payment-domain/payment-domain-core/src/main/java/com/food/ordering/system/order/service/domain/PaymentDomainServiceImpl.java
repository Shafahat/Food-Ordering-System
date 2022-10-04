package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.order.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.order.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.order.service.domain.event.PaymentEvent;
import com.food.ordering.system.order.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.order.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.order.service.domain.valueobject.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.food.ordering.system.domain.constants.DomainConstants.UTC;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService {
    @Override
    public PaymentEvent validateAndInitializePayment(Payment payment,
                                                     CreditEntry creditEntry,
                                                     List<CreditHistory> creditHistory,
                                                     List<String> failureMessages) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistory, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistory, failureMessages);

        if (failureMessages.isEmpty()) {
            log.info("Payment is valid and initialized");
            payment.updateStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        } else {
            log.info("Payment is invalid and not initialized");
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }

    }

    @Override
    public PaymentEvent validateAndCancelledPayment(Payment payment,
                                                    CreditEntry creditEntry,
                                                    List<CreditHistory> creditHistory,
                                                    List<String> failureMessages) {

        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistory, TransactionType.CREDIT);

        if (failureMessages.isEmpty()) {
            log.info("Payment is valid and cancelled");
            payment.updateStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)));
        } else {
            log.info("Payment is invalid and not cancelled");
            payment.updateStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(UTC)), failureMessages);
        }
    }


    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if (payment.getPrice().isGreaterThan(creditEntry.getTotalCreditAmount())) {
            failureMessages.add("Customer id " + payment.getCustomerId().getValue()
                    + " , has insufficient credit amount" + creditEntry.getTotalCreditAmount().getAmount()
                    + " to pay for order id " + payment.getOrderId().getValue());
            log.error("Payment price is greater than credit");
        }
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void updateCreditHistory(Payment payment,
                                     List<CreditHistory> creditHistory,
                                     TransactionType transactionType) {
        creditHistory.add(
                CreditHistory.builder()
                        .id(new CreditHistoryId(UUID.randomUUID()))
                        .customerId(payment.getCustomerId())
                        .amount(payment.getPrice())
                        .transactionType(transactionType)
                        .build());
    }


    private void validateCreditHistory(CreditEntry creditEntry,
                                       List<CreditHistory> creditHistory,
                                       List<String> failureMessages) {
        Money totalCreditHistory = getTotalHistoryAmount(creditHistory, TransactionType.CREDIT);
        Money totalDebitHistory = getTotalHistoryAmount(creditHistory, TransactionType.DEBIT);

        if (totalDebitHistory.isGreaterThan(totalCreditHistory)) {
            failureMessages.add("Customer id " + creditEntry.getCustomerId().getValue() + " has insufficient credit");
            log.error("Customer id {} has insufficient credit", creditEntry.getCustomerId().getValue());
        }

        if (!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.subtract(totalDebitHistory))) {
            failureMessages.add("Customer id " + creditEntry.getCustomerId().getValue()
                    + " has total is not equal to credit history");
            log.error("Customer id {} has total is not equal to credit history", creditEntry.getCustomerId().getValue());
        }
    }

    private Money getTotalHistoryAmount(List<CreditHistory> creditHistory, TransactionType transactionType) {
        return creditHistory.stream()
                .filter(history -> transactionType.equals(history.getTransactionType()))
                .map(CreditHistory::getAmount)
                .reduce(Money.ZERO, Money::add);
    }
}
