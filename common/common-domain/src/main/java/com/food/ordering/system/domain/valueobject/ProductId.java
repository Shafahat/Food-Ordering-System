package com.food.ordering.system.domain.valueobject;

import java.util.UUID;

public final class ProductId extends BaseId<UUID> {
    public ProductId(UUID value) {
        super(value);
    }
}
