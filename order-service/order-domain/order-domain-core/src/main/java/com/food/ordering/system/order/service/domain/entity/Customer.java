package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;

public class Customer extends AggregateRoot<CustomerId> {
    private String username;
    private String firstName;
    private String lastName;

    public Customer(CustomerId id, String username, String firstName, String lastName) {
        super.setId(id);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Customer(CustomerId id) {
        super.setId(id);
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
