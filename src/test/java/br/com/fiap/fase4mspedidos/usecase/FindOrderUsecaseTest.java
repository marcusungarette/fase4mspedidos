package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FindOrderUsecaseTest {

    @Mock
    private OrderGateway orderGateway;

    @InjectMocks
    private FindOrderUsecase usecase;

    private Order mockOrder1;
    private Order mockOrder2;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        List<OrderItem> items1 = Arrays.asList(
                new OrderItem(1L, "SKU-001", 2, new BigDecimal("100.00"), new BigDecimal("200.00")),
                new OrderItem(2L, "SKU-002", 1, new BigDecimal("50.00"), new BigDecimal("50.00"))
        );

        mockOrder1 = new Order(
                1L,
                101L,
                items1,
                "4111111111111111",
                OrderStatus.ABERTO,
                new BigDecimal("250.00"),
                now.minusHours(1),
                now.minusHours(1)
        );

        List<OrderItem> items2 = Arrays.asList(
                new OrderItem(3L, "SKU-003", 1, new BigDecimal("300.00"), new BigDecimal("300.00"))
        );

        mockOrder2 = new Order(
                2L,
                102L,
                items2,
                "4222222222222222",
                OrderStatus.FECHADO_COM_SUCESSO,
                new BigDecimal("300.00"),
                now.minusHours(2),
                now.minusHours(2)
        );
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        when(orderGateway.findById(1L)).thenReturn(Optional.of(mockOrder1));

        Order result = usecase.findById(1L);

        assertEquals(mockOrder1.getId(), result.getId());
        assertEquals(mockOrder1.getCustomerId(), result.getCustomerId());
        assertEquals(mockOrder1.getStatus(), result.getStatus());
        assertEquals(mockOrder1.getTotalAmount(), result.getTotalAmount());
        assertEquals(mockOrder1.getItems().size(), result.getItems().size());
    }

    @Test
    void findById_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
        when(orderGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> usecase.findById(999L));
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        List<Order> mockOrders = Arrays.asList(mockOrder1, mockOrder2);
        when(orderGateway.findAll()).thenReturn(mockOrders);

        List<Order> result = usecase.findAll();

        assertEquals(2, result.size());
        assertEquals(mockOrder1.getId(), result.get(0).getId());
        assertEquals(mockOrder2.getId(), result.get(1).getId());
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoOrdersExist() {
        when(orderGateway.findAll()).thenReturn(List.of());

        List<Order> result = usecase.findAll();

        assertEquals(0, result.size());
    }
}