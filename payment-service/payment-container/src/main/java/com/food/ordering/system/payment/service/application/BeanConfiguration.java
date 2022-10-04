package com.food.ordering.system.payment.service.application;

import com.food.ordering.system.order.service.domain.PaymentDomainService;
import com.food.ordering.system.order.service.domain.PaymentDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Bean
    public PaymentDomainService paymentDomainService() {
        return new PaymentDomainServiceImpl();
    }
}
