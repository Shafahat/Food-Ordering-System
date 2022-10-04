package com.food.ordering.system.order.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.food.ordering.system.order.service.data.access",
        "com.food.ordering.system.common.data.access"})
@EntityScan(basePackages = {"com.food.ordering.system.order.service.data.access",
        "com.food.order.system.common.data.access"})
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}


