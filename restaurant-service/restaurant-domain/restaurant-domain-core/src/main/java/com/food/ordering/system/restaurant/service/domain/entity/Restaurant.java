package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;

import java.util.List;
import java.util.UUID;

public class Restaurant extends AggregateRoot<RestaurantId> {
    private final OrderDetail orderDetail;
    private OrderApproval orderApproval;
    private boolean active;

    private Restaurant(Builder builder) {
        setId(builder.restaurantId);
        orderApproval = builder.orderApproval;
        active = builder.active;
        orderDetail = builder.orderDetail;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void validateOrder(List<String> failureMessages) {
        if (!orderDetail.getStatus().equals(OrderStatus.PAID)) {
            failureMessages.add("Order is not paid");
        }
        Money totalAmount = orderDetail.getProducts()
                .stream()
                .map(product -> {
                    if (Boolean.FALSE.equals(product.isAvailable())) {
                        failureMessages.add("Product is not available");
                    }
                    return product.getPrice().multiply(product.getQuantity());
                })
                .reduce(Money.ZERO, Money::add);

        if (!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Total amount is not correct");
        }
    }

    public void constructOrderApproval(OrderApprovalStatus status) {
        this.orderApproval = OrderApproval.builder()
                .id(new OrderApprovalId(UUID.randomUUID()))
                .restaurantId(this.getId())
                .orderId(this.getOrderDetail().getId())
                .status(status)
                .build();
    }

    public OrderApproval getOrderApproval() {
        return orderApproval;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public static final class Builder {
        private RestaurantId restaurantId;
        private OrderApproval orderApproval;
        private OrderDetail orderDetail;
        private boolean active;

        private Builder() {
        }

        public Builder id(RestaurantId val) {
            restaurantId = val;
            return this;
        }

        public Builder orderApproval(OrderApproval val) {
            orderApproval = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Builder orderDetail(OrderDetail val) {
            orderDetail = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}

