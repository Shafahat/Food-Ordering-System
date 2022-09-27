package com.food.ordering.system.order.sercvice.domain;

import com.food.ordering.system.order.sercvice.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.sercvice.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.sercvice.domain.ports.input.message.listener.payment.PaymentResponseMessageListener;
import com.food.ordering.system.order.sercvice.domain.ports.input.message.listener.restaurant_approval.RestaurantApprovalResponseMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class RestaurantApprovalResponseMessageListenerImpl implements RestaurantApprovalResponseMessageListener {
    @Override
    public void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse) {

    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {

    }
}
