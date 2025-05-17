package br.com.fiap.fase4mspedidos.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void shouldCreateOrderItemWithAllProperties() {
        Long id = 1L;
        String sku = "SKU-12345";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("10.50");
        BigDecimal subtotal = new BigDecimal("21.00");

        OrderItem orderItem = new OrderItem(id, sku, quantity, unitPrice, subtotal);

        assertEquals(id, orderItem.getId());
        assertEquals(sku, orderItem.getSku());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(unitPrice, orderItem.getUnitPrice());
        assertEquals(subtotal, orderItem.getSubtotal());
    }

    @Test
    void shouldCreateOrderItemWithNullId() {
        Long id = null;
        String sku = "SKU-12345";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("10.50");
        BigDecimal subtotal = new BigDecimal("21.00");

        OrderItem orderItem = new OrderItem(id, sku, quantity, unitPrice, subtotal);

        assertNull(orderItem.getId());
        assertEquals(sku, orderItem.getSku());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(unitPrice, orderItem.getUnitPrice());
        assertEquals(subtotal, orderItem.getSubtotal());
    }

    @Test
    void shouldCreateOrderItemWithMinimalConstructor() {
        String sku = "SKU-12345";
        Integer quantity = 3;
        BigDecimal unitPrice = new BigDecimal("10.50");
        BigDecimal expectedSubtotal = new BigDecimal("31.50");

        OrderItem orderItem = new OrderItem(sku, quantity, unitPrice);

        assertNull(orderItem.getId());
        assertEquals(sku, orderItem.getSku());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(unitPrice, orderItem.getUnitPrice());
        assertEquals(0, expectedSubtotal.compareTo(orderItem.getSubtotal()));
    }

    @Test
    void shouldCalculateSubtotalCorrectly() {
        String sku = "SKU-12345";
        Integer quantity = 5;
        BigDecimal unitPrice = new BigDecimal("19.99");
        BigDecimal expectedSubtotal = new BigDecimal("99.95");

        OrderItem orderItem = new OrderItem(sku, quantity, unitPrice);

        assertEquals(0, expectedSubtotal.compareTo(orderItem.getSubtotal()));
    }

    @Test
    void shouldHandleZeroQuantity() {
        String sku = "SKU-12345";
        Integer quantity = 0;
        BigDecimal unitPrice = new BigDecimal("10.50");
        BigDecimal expectedSubtotal = BigDecimal.ZERO;

        OrderItem orderItem = new OrderItem(sku, quantity, unitPrice);

        assertEquals(0, expectedSubtotal.compareTo(orderItem.getSubtotal()));
    }

    @Test
    void shouldHandleHighPrecisionPrice() {
        String sku = "SKU-12345";
        Integer quantity = 3;
        BigDecimal unitPrice = new BigDecimal("10.505");
        BigDecimal expectedSubtotal = new BigDecimal("31.515");

        OrderItem orderItem = new OrderItem(sku, quantity, unitPrice);

        assertEquals(0, expectedSubtotal.compareTo(orderItem.getSubtotal()));
    }

    @Test
    void shouldHandleLargeQuantity() {
        String sku = "SKU-12345";
        Integer quantity = 10000;
        BigDecimal unitPrice = new BigDecimal("10.50");
        BigDecimal expectedSubtotal = new BigDecimal("105000.00");

        OrderItem orderItem = new OrderItem(sku, quantity, unitPrice);

        assertEquals(0, expectedSubtotal.compareTo(orderItem.getSubtotal()));
    }
}