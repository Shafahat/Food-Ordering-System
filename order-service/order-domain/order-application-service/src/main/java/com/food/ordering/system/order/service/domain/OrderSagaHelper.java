package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaHelper {
    private final OrderRepository repository;

    public Order findOrder(String orderId) {
        return repository.findById(new OrderId(UUID.fromString(orderId)))
                .orElseThrow(() -> new OrderNotFoundException("Order not found -> Order id :" + orderId));
    }

    public void saveOrder(Order order) {
        repository.save(order);
    }

//    public SagaStatus orderStatusToSagaStatus(OrderStatus orderStatus) {
//        return switch (orderStatus) {
//            case PAID -> SagaStatus.PROCESSING;
//            case APPROVED -> SagaStatus.SUCCEEDED;
//            case CANCELLING -> SagaStatus.COMPENSATING;
//            case CANCELLED -> SagaStatus.COMPENSATED;
//            default -> SagaStatus.STARTED;
//        };
//    }
}


