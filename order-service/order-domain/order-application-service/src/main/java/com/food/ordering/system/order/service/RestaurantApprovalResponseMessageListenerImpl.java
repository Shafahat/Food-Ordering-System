package com.food.ordering.system.order.service;

import com.food.ordering.system.order.service.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.ports.input.message.listener.restaurant_approval.RestaurantApprovalResponseMessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {
    private final OrderApprovalSaga approvalSaga;

    @Override
    public void orderApproved(RestaurantApprovalResponse response) {
        approvalSaga.process(response);
        log.info("Order Approved: {}", response.getId());
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse response) {
        approvalSaga.rollback(response);
        log.info("Order Rejected: {}", response.getId());
    }
}
