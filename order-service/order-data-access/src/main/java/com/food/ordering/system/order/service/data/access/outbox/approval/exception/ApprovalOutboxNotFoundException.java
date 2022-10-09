package com.food.ordering.system.order.service.data.access.outbox.approval.exception;

public class ApprovalOutboxNotFoundException extends RuntimeException {
    public ApprovalOutboxNotFoundException(String message) {
        super(message);
    }
}
