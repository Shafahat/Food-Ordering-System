package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

public final class RestaurantId extends BaseId<UUID> {
    public RestaurantId(UUID value) {
        super(value);
    }
}
