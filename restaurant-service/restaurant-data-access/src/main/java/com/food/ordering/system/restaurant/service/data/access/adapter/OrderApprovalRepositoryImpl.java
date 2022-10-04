package com.food.ordering.system.restaurant.service.data.access.adapter;

import com.food.ordering.system.restaurant.service.data.access.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.data.access.repository.OrderApprovalJpaRepository;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {
    private final OrderApprovalJpaRepository jpaRepository;
    private final RestaurantDataAccessMapper mapper;

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        return mapper.mapToOrderApproval(jpaRepository.save(mapper.mapToOrderApprovalEntity(orderApproval)));
    }
}
