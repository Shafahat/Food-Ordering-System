package com.food.ordering.system.restaurant.service.data.access.repository;

import com.food.ordering.system.restaurant.service.data.access.entity.OrderApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderApprovalJpaRepository extends JpaRepository<OrderApprovalEntity, UUID> {
}
