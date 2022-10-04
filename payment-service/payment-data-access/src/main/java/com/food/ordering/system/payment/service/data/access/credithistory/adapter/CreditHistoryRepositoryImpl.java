package com.food.ordering.system.payment.service.data.access.credithistory.adapter;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.data.access.credithistory.entity.CreditHistoryEntity;
import com.food.ordering.system.payment.service.data.access.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.food.ordering.system.payment.service.data.access.credithistory.repository.CreditHistoryJpaRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {
    private final CreditHistoryJpaRepository jpaRepository;
    private final CreditHistoryDataAccessMapper mapper;

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        return mapper.mapToCreditHistory(jpaRepository.save(mapper.mapToCreditHistoryEntity(creditHistory)));
    }


    @Override
    public Optional<List<CreditHistory>> findByCustomerId(CustomerId customerId) {
        Optional<List<CreditHistoryEntity>> creditHistory = jpaRepository.findByCustomerId(customerId.getValue());
        return creditHistory.map(creditHistoryList -> creditHistoryList.stream()
                .map(mapper::mapToCreditHistory)
                .collect(Collectors.toList()));
    }
}
