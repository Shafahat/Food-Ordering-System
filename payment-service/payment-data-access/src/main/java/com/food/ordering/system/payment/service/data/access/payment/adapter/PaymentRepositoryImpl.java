package com.food.ordering.system.payment.service.data.access.payment.adapter;

import com.food.ordering.system.payment.service.data.access.payment.mapper.PaymentDataAccessMapper;
import com.food.ordering.system.payment.service.data.access.payment.repository.PaymentJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;
    private final PaymentDataAccessMapper mapper;

    @Override
    public Payment save(Payment payment) {
        return mapper.mapToPayment(jpaRepository.save(mapper.mapToPaymentEntity(payment)));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::mapToPayment);
    }
}