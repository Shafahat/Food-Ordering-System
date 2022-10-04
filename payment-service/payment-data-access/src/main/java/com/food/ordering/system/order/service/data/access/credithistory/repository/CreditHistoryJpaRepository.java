package com.food.ordering.system.order.service.data.access.credithistory.repository;

import com.food.ordering.system.order.service.data.access.credithistory.entity.CreditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditHistoryJpaRepository extends JpaRepository<CreditHistoryEntity, UUID> {
    Optional<List<CreditHistoryEntity>> findByCustomerId(UUID customerId);
}

