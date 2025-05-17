package br.com.fiap.fase4mspedidos.controller.mapper;

import br.com.fiap.fase4mspedidos.controller.dto.OrderItemRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderItemResponseDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderResponseDTO;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void shouldMapOrderRequestDTOToDomain() {
        OrderRequestDTO requestDTO = new OrderRequestDTO(
                100L,
                Arrays.asList(
                        new OrderItemRequestDTO("SKU-101", 2),
                        new OrderItemRequestDTO("SKU-102", 3)
                ),
                "1234-5678-9012-3456"
        );

        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new OrderItem(2L, "SKU-102", 3, new BigDecimal("15.00"), new BigDecimal("45.00"))
        );

        BigDecimal totalAmount = new BigDecimal("65.00");

        Order result = orderMapper.toDomain(requestDTO, items, totalAmount);

        assertNull(result.getId());
        assertEquals(100L, result.getCustomerId());
        assertEquals(items, result.getItems());
        assertEquals("1234-5678-9012-3456", result.getCreditCardNumber());
        assertEquals(OrderStatus.ABERTO, result.getStatus());
        assertEquals(totalAmount, result.getTotalAmount());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void shouldMapOrderItemRequestDTOToDomain() {
        OrderItemRequestDTO requestDTO = new OrderItemRequestDTO("SKU-101", 2);
        BigDecimal unitPrice = new BigDecimal("10.00");

        OrderItem result = orderMapper.toOrderItem(requestDTO, unitPrice);

        assertNull(result.getId());
        assertEquals("SKU-101", result.getSku());
        assertEquals(2, result.getQuantity());
        assertEquals(unitPrice, result.getUnitPrice());
        assertEquals(0, new BigDecimal("20.00").compareTo(result.getSubtotal()));
    }

    @Test
    void shouldMapOrderToResponseDTO() {
        Long orderId = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00")),
                new OrderItem(2L, "SKU-102", 3, new BigDecimal("15.00"), new BigDecimal("45.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("65.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(orderId, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        OrderResponseDTO result = orderMapper.toResponseDTO(order);

        assertEquals(orderId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(2, result.getItems().size());
        assertEquals(status, result.getStatus());
        assertEquals(totalAmount, result.getTotalAmount());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());

        OrderItemResponseDTO item1 = result.getItems().get(0);
        assertEquals(1L, item1.getId());
        assertEquals("SKU-101", item1.getSku());
        assertEquals(2, item1.getQuantity());
        assertEquals(0, new BigDecimal("10.00").compareTo(item1.getUnitPrice()));
        assertEquals(0, new BigDecimal("20.00").compareTo(item1.getSubtotal()));

        OrderItemResponseDTO item2 = result.getItems().get(1);
        assertEquals(2L, item2.getId());
        assertEquals("SKU-102", item2.getSku());
        assertEquals(3, item2.getQuantity());
        assertEquals(0, new BigDecimal("15.00").compareTo(item2.getUnitPrice()));
        assertEquals(0, new BigDecimal("45.00").compareTo(item2.getSubtotal()));
    }

    @Test
    void shouldMapOrderWithEmptyItemsToResponseDTO() {
        Long orderId = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Collections.emptyList();
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(orderId, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        OrderResponseDTO result = orderMapper.toResponseDTO(order);

        assertEquals(orderId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertTrue(result.getItems().isEmpty());
        assertEquals(status, result.getStatus());
        assertEquals(totalAmount, result.getTotalAmount());
    }

    @Test
    void shouldMapOrderWithDifferentStatusToResponseDTO() {
        Long orderId = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.FECHADO_COM_SUCESSO;
        BigDecimal totalAmount = new BigDecimal("20.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusHours(1);

        Order order = new Order(orderId, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        OrderResponseDTO result = orderMapper.toResponseDTO(order);

        assertEquals(status, result.getStatus());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}