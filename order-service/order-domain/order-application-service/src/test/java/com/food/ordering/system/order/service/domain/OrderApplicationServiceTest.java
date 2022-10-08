package com.food.ordering.system.order.service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentOrderStatus;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItemDto;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.entity.Customer;
import com.food.ordering.system.payment.service.domain.entity.Order;
import com.food.ordering.system.payment.service.domain.entity.Product;
import com.food.ordering.system.payment.service.domain.entity.Restaurant;
import com.food.ordering.system.payment.service.domain.exception.OrderDomainException;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {
    private final UUID CUSTOMER_ID = UUID.fromString("8aabd9de-1292-4402-9ae0-bc955119f734");
    private final UUID RESTAURANT_ID = UUID.fromString("ae0c340a-e11e-4660-92ea-8279d24ac545");
    private final UUID PRODUCT_ID = UUID.fromString("f1efc22f-25c7-4754-83af-b7e6ccdf7ab5");
    private final UUID ORDER_ID = UUID.fromString("2019f8b8-8623-42d0-9991-a37517de41b5");
    private final UUID SAGA_ID = UUID.fromString("2019f8b8-8623-42d0-9991-a37517de41b4");
    private final BigDecimal PRICE = new BigDecimal("200.00");
    private final BigDecimal WRONG_PRICE = new BigDecimal("250.00");
    private final BigDecimal WRONG_PRODUCT_PRICE = new BigDecimal("210.00");

    @Autowired
    private OrderApplicationService orderApplicationService;
    @Autowired
    private OrderDataMapper orderDataMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;
    private Product product1;
    private Product product2;

    @BeforeAll
    public void init() {
        OrderAddress orderAddress = OrderAddress.builder()
                .street("street-1")
                .postalCode("100AB")
                .city("Paris")
                .build();

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("50.00"))
                .build();

        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .quantity(3)
                .price(new BigDecimal("50.00"))
                .subTotal(new BigDecimal("150.00"))
                .build();

        OrderItemDto orderItemDtoWithWrongPrice = OrderItemDto.builder()
                .productId(PRODUCT_ID)
                .quantity(1)
                .price(new BigDecimal("60.00"))
                .subTotal(new BigDecimal("60.00"))
                .build();

        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(orderAddress)
                .price(PRICE)
                .items(List.of(orderItemDto, orderItemDto2))
                .build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(orderAddress)
                .price(WRONG_PRICE)
                .items(List.of(orderItemDto, orderItemDto2))
                .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(orderAddress)
                .price(WRONG_PRODUCT_PRICE)
                .items(List.of(orderItemDtoWithWrongPrice, orderItemDto2))
                .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Money productPrice = new Money(new BigDecimal("50.00"));

        product1 = new Product(new ProductId(PRODUCT_ID), "product-1",
                productPrice);
        product2 = new Product(new ProductId(PRODUCT_ID), "product-2",
                productPrice);

        Restaurant restaurant = Restaurant.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(product1, product2))
                .active(true)
                .build();

        Order order = orderDataMapper.mapToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        when(restaurantRepository.findRestaurantInformation(orderDataMapper
                .mapToRestaurant(createOrderCommand))).thenReturn(Optional.of(restaurant));

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        when(paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class))).thenReturn(getOrderPaymentOutboxMessage());
    }

    @Test
    public void testCreateOrder() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
        assertNotNull(createOrderResponse.getOrderTrackingId());
    }

    @Test
    public void testCreateOrderWithWrongTotalPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
        assertEquals(orderDomainException.getMessage(),
                "Order total price is not equal to the sum of order items prices");
    }

    @Test
    public void testCreateOrderWithWrongProductPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));

        assertEquals(orderDomainException.getMessage(),
                "Order item price is not valid");
    }

    @Test
    public void testCreateOrderWithPassiveRestaurant() {
        Restaurant passiveRestaurant = Restaurant.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(product1, product2))
                .active(false)
                .build();

        when(restaurantRepository.findRestaurantInformation(orderDataMapper
                .mapToRestaurant(createOrderCommand))).thenReturn(Optional.of(passiveRestaurant));

        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
        assertEquals(orderDomainException.getMessage(),
                "Restaurant is not active, please try again later. " +
                        "Restaurant id: " + passiveRestaurant.getId().getValue());
    }


    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
        OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
                .orderId(ORDER_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(PRICE)
                .createdAt(ZonedDateTime.now())
                .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
                .build();

        return OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(SAGA_ID)
                .createdAt(ZonedDateTime.now())
                .type(ORDER_SAGA_NAME)
                .payload(createPayload(orderPaymentEventPayload))
                .orderStatus(OrderStatus.PENDING)
                .sagaStatus(SagaStatus.STARTED)
                .outboxStatus(OutboxStatus.STARTED)
                .version(0)
                .build();
    }

    private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Cannot create OrderPaymentEventPayload object!");
        }
    }
}
