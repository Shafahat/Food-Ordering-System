package com.food.ordering.system.order.sercvice.domain.dto.message;

import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;

import java.time.Instant;
import java.util.List;

public class RestaurantApprovalResponse {
    private String id;
    private String sagaId;
    private String orderId;
    private String restaurantId;
    private String customerId;
    private Instant createdAt;
    private OrderApprovalStatus orderApprovalStatus ;
    private List<String> failureMessages;
}
