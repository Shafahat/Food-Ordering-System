package com.food.ordering.system.customer.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableJpaRepositories(basePackages = {"com.food.ordering.system.customer.service",
//        "com.food.ordering.system.common.data.access"})
//@EntityScan(basePackages = {"com.food.ordering.system.customer.data.access",
//        "com.food.order.system.common.data.access"})
@SpringBootApplication(scanBasePackages = "com.food.ordering.system")
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}