package com.food.ordering.system.customer.service.domain.create;

import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Builder
public record CreateCustomerResponse(@NotNull UUID customerId, @NotNull String message) {
}
