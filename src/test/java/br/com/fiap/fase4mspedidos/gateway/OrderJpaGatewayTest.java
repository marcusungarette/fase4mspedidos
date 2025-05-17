package br.com.fiap.fase4mspedidos.gateway.database.jpa;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.gateway.database.jpa.entity.OrderEntity;
import br.com.fiap.fase4mspedidos.gateway.database.jpa.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderJpaGatewayTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderJpaGateway orderJpaGateway;

    @Test
    void shouldSaveOrderSuccessfully() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("20.00");
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order(null, customerId, items, creditCardNumber, status, totalAmount, now, now);

        OrderEntity entity = mock(OrderEntity.class);
        OrderEntity savedEntity = mock(OrderEntity.class);

        Order expectedOrder = new Order(id, customerId, items, creditCardNumber, status, totalAmount, now, now);

        try (MockedStatic<OrderEntity> mockedStatic = mockStatic(OrderEntity.class)) {
            mockedStatic.when(() -> OrderEntity.fromDomain(any(Order.class))).thenReturn(entity);

            when(orderRepository.save(entity)).thenReturn(savedEntity);
            when(savedEntity.toDomain()).thenReturn(expectedOrder);

            Order result = orderJpaGateway.save(order);

            assertNotNull(result);
            assertEquals(id, result.getId());
            assertEquals(customerId, result.getCustomerId());
            assertEquals(items, result.getItems());
            assertEquals(creditCardNumber, result.getCreditCardNumber());
            assertEquals(status, result.getStatus());
            assertEquals(totalAmount, result.getTotalAmount());

            verify(orderRepository).save(entity);
            verify(savedEntity).toDomain();
            mockedStatic.verify(() -> OrderEntity.fromDomain(order));
        }
    }

    @Test
    void shouldFindOrderById() {
        Long orderId = 1L;
        OrderEntity entity = mock(OrderEntity.class);

        LocalDateTime now = LocalDateTime.now();
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        Order expectedOrder = new Order(
                orderId,
                100L,
                items,
                "1234-5678-9012-3456",
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(entity));
        when(entity.toDomain()).thenReturn(expectedOrder);

        Optional<Order> result = orderJpaGateway.findById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        assertEquals(100L, result.get().getCustomerId());
        assertEquals(items, result.get().getItems());

        verify(orderRepository).findById(orderId);
        verify(entity).toDomain();
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = orderJpaGateway.findById(orderId);

        assertFalse(result.isPresent());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldFindAllOrders() {
        OrderEntity entity1 = mock(OrderEntity.class);
        OrderEntity entity2 = mock(OrderEntity.class);

        LocalDateTime now = LocalDateTime.now();

        Order order1 = new Order(
                1L,
                100L,
                Collections.singletonList(new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))),
                "1234-5678-9012-3456",
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        Order order2 = new Order(
                2L,
                200L,
                Collections.singletonList(new OrderItem(2L, "SKU-102", 1, new BigDecimal("15.00"), new BigDecimal("15.00"))),
                "9876-5432-1098-7654",
                OrderStatus.FECHADO_COM_SUCESSO,
                new BigDecimal("15.00"),
                now,
                now
        );

        when(orderRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));
        when(entity1.toDomain()).thenReturn(order1);
        when(entity2.toDomain()).thenReturn(order2);

        List<Order> result = orderJpaGateway.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(100L, result.get(0).getCustomerId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(200L, result.get(1).getCustomerId());

        verify(orderRepository).findAll();
        verify(entity1).toDomain();
        verify(entity2).toDomain();
    }

    @Test
    void shouldDeleteOrderById() {
        Long orderId = 1L;
        doNothing().when(orderRepository).deleteById(orderId);

        orderJpaGateway.deleteById(orderId);

        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void shouldHandleEmptyFindAllResult() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<Order> result = orderJpaGateway.findAll();

        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
    }

    @Test
    void shouldSaveOrderWithUpdatedStatus() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.FECHADO_COM_SUCESSO;
        BigDecimal totalAmount = new BigDecimal("20.00");
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, now, now);

        OrderEntity entity = mock(OrderEntity.class);
        OrderEntity savedEntity = mock(OrderEntity.class);

        Order expectedOrder = new Order(id, customerId, items, creditCardNumber, status, totalAmount, now, now);

        try (MockedStatic<OrderEntity> mockedStatic = mockStatic(OrderEntity.class)) {
            mockedStatic.when(() -> OrderEntity.fromDomain(any(Order.class))).thenReturn(entity);

            when(orderRepository.save(entity)).thenReturn(savedEntity);
            when(savedEntity.toDomain()).thenReturn(expectedOrder);

            Order result = orderJpaGateway.save(order);

            assertNotNull(result);
            assertEquals(status, result.getStatus());

            verify(orderRepository).save(entity);
            verify(savedEntity).toDomain();
            mockedStatic.verify(() -> OrderEntity.fromDomain(order));
        }
    }
}