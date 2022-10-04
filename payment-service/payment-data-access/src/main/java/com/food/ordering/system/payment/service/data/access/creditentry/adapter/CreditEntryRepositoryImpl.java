package com.food.ordering.system.payment.service.data.access.creditentry.adapter;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.data.access.creditentry.mapper.CreditEntryDataAccessMapper;
import com.food.ordering.system.payment.service.data.access.creditentry.repository.CreditEntryJpaRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class CreditEntryRepositoryImpl implements CreditEntryRepository {
    private final CreditEntryJpaRepository jpaRepository;
    private final CreditEntryDataAccessMapper mapper;

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        return mapper.mapToCreditEntry(jpaRepository.save(mapper.mapToCreditEntryEntity(creditEntry)));
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue()).map(mapper::mapToCreditEntry);
    }
}