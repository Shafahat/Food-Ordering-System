package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.exception.DomainException;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.entity.Restaurant;
import com.food.ordering.system.payment.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.payment.service.domain.exception.OrderDomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreateHelper {
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;

    @Transactional
    public OrderCreatedEvent persistOrder(CreateOrderCommand createOrderCommand) {
        log.info("{}", createOrderCommand.getPrice());
        log.info("{}", createOrderCommand.getCustomerId());
        log.info("{}", createOrderCommand.getRestaurantId());
        log.warn("{}", createOrderCommand.getItems().get(0).getPrice() + ", "
                + createOrderCommand.getItems().get(0).getProductId() + ", "
                + createOrderCommand.getItems().get(0).getQuantity() + ", "
                + createOrderCommand.getItems().get(0).getSubTotal());
        log.warn("{}", createOrderCommand.getItems().get(1).getPrice() + ", "
                + createOrderCommand.getItems().get(1).getProductId() + ", "
                + createOrderCommand.getItems().get(1).getQuantity() + ", "
                + createOrderCommand.getItems().get(1).getSubTotal());


        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        Order order = orderDataMapper.mapToOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);
        saveOrder(order);
        log.info("Order is created with id: {}", orderCreatedEvent.getOrder().getId().getValue());
        return orderCreatedEvent;
    }

    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        return restaurantRepository
                .findRestaurantInformation(orderDataMapper.mapToRestaurant(createOrderCommand))
                .orElseThrow(() -> new OrderDomainException("Could not find restaurant with restaurant id: "
                        + createOrderCommand.getRestaurantId()));
    }

    private void checkCustomer(UUID customerId) {
        customerRepository.findCustomer(customerId)
                .orElseThrow(() -> new OrderDomainException("Could not find customer with customer id:" + customerId));
    }

    private Order saveOrder(Order order) {
        Order orderResult = orderRepository.save(order);
        if (orderResult == null) {
            log.error("Could not save order!");
            throw new DomainException("Could not save order!");
        }
        log.info("Order saved with id: {}", orderResult.getId().getValue());
        return orderResult;
    }
}
