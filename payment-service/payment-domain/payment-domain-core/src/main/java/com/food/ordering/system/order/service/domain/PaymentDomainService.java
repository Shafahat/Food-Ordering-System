package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.entity.CreditEntry;
import com.food.ordering.system.order.service.domain.entity.CreditHistory;
import com.food.ordering.system.order.service.domain.entity.Payment;
import com.food.ordering.system.order.service.domain.event.PaymentEvent;

import java.util.List;

public interface PaymentDomainService {
    PaymentEvent validateAndInitializePayment(Payment payment, CreditEntry creditEntry,
                                              List<CreditHistory> creditHistory, List<String> failureMessages);

    PaymentEvent validateAndCancelledPayment(Payment payment, CreditEntry creditEntry,
                                             List<CreditHistory> creditHistory, List<String> failureMessages);
}
