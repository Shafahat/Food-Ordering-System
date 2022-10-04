package com.food.ordering.system.restaurant.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.food.ordering.system.restaurant.service.data.access",
        "com.food.ordering.system.common.data.access"})
@EntityScan(basePackages = {"com.food.ordering.system.restaurant.service.data.access",
        "com.food.ordering.system.common.data.access"})
@SpringBootApplication(scanBasePackages = "com.food.ordering")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
