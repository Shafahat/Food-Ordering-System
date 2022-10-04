package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Payment extends AggregateRoot<PaymentId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money price;
    private PaymentStatus status;
    private ZonedDateTime createdAt;

    private Payment(Builder builder) {
        super.setId(builder.paymentId);
        orderId = builder.orderId;
        customerId = builder.customerId;
        price = builder.price;
        status = builder.status;
        createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void initializePayment() {
        setId(new PaymentId(UUID.randomUUID()));
        createdAt = ZonedDateTime.now(ZoneId.of("UFC"));
    }

    public void validatePayment(List<String> failureMessages) {
        if (Objects.isNull(price) || !price.isGreaterThanZero()) {
            failureMessages.add("Payment price must be greater than zero");
        }
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getPrice() {
        return price;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private PaymentId paymentId;
        private OrderId orderId;
        private CustomerId customerId;
        private Money price;
        private PaymentStatus status;
        private ZonedDateTime createdAt;

        private Builder() {
        }

        public Builder id(PaymentId val) {
            paymentId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder status(PaymentStatus val) {
            status = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
