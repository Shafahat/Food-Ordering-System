package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

public final class OrderId extends BaseId<UUID>{
    public OrderId(UUID value) {
        super(value);
    }
}
