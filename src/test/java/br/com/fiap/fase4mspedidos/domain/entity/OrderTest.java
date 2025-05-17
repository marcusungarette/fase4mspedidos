package br.com.fiap.fase4mspedidos.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateOrderWithAllProperties() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.50"), new BigDecimal("21.00")),
                new OrderItem(2L, "SKU-102", 1, new BigDecimal("15.75"), new BigDecimal("15.75"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("36.75");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(items, order.getItems());
        assertEquals(creditCardNumber, order.getCreditCardNumber());
        assertEquals(status, order.getStatus());
        assertEquals(totalAmount, order.getTotalAmount());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(updatedAt, order.getUpdatedAt());
    }

    @Test
    void shouldCreateOrderWithNullId() {
        Long id = null;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(null, "SKU-101", 2, new BigDecimal("10.50"), new BigDecimal("21.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("21.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertNull(order.getId());
        assertEquals(customerId, order.getCustomerId());
    }

    @Test
    void shouldCreateOrderWithEmptyItems() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = new ArrayList<>();
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertTrue(order.getItems().isEmpty());
        assertEquals(totalAmount, order.getTotalAmount());
    }

    @Test
    void shouldCreateOrderWithOrderStatusFechadoComSucesso() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.50"), new BigDecimal("21.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.FECHADO_COM_SUCESSO;
        BigDecimal totalAmount = new BigDecimal("21.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusHours(1);

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(status, order.getStatus());
        assertEquals(updatedAt, order.getUpdatedAt());
        assertNotEquals(createdAt, order.getUpdatedAt());
    }

    @Test
    void shouldCreateOrderWithOrderStatusFechadoSemEstoque() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.50"), new BigDecimal("21.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.FECHADO_SEM_ESTOQUE;
        BigDecimal totalAmount = new BigDecimal("21.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusHours(1);

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(status, order.getStatus());
    }

    @Test
    void shouldCreateOrderWithOrderStatusFechadoSemCredito() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.50"), new BigDecimal("21.00"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.FECHADO_SEM_CREDITO;
        BigDecimal totalAmount = new BigDecimal("21.00");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusHours(1);

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(status, order.getStatus());
    }

    @Test
    void shouldCreateOrderWithLargeAmount() {
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 1, new BigDecimal("9999999.99"), new BigDecimal("9999999.99"))
        );
        String creditCardNumber = "1234-5678-9012-3456";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("9999999.99");
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt;

        Order order = new Order(id, customerId, items, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        assertEquals(totalAmount, order.getTotalAmount());
        assertEquals(0, totalAmount.compareTo(order.getTotalAmount()));
    }
}