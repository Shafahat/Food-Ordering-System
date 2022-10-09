package com.food.ordering.system.order.service.container;

import com.food.ordering.system.order.test.OrderDomainService;
import com.food.ordering.system.order.test.OrderDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
