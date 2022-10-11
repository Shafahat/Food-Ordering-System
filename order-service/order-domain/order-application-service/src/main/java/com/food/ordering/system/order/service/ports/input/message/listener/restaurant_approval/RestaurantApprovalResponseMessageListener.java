package com.food.ordering.system.order.service.ports.input.message.listener.restaurant_approval;

import com.food.ordering.system.order.service.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalResponseMessageListener {
    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
