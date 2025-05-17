package br.com.fiap.fase4mspedidos.gateway.database.jpa.entity;

import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemEntityTest {

    @Test
    void shouldConvertFromDomainToEntity() {
        Long id = 1L;
        String sku = "SKU-101";
        Integer quantity = 2;
        BigDecimal unitPrice = new BigDecimal("10.00");
        BigDecimal subtotal = new BigDecimal("20.00");

        OrderItem orderItem = new OrderItem(id, sku, quantity, unitPrice, subtotal);

        OrderItemEntity entity = new OrderItemEntity(orderItem);

        assertEquals(id, entity.getId());
        assertEquals(sku, entity.getSku());
        assertEquals(quantity, entity.getQuantity());
        assertEquals(unitPrice, entity.getUnitPrice());
        assertEquals(subtotal, entity.getSubtotal());
        assertNull(entity.getOrder());
    }

    @Test
    void shouldConvertFromEntityToDomain() {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(1L);
        entity.setSku("SKU-101");
        entity.setQuantity(2);
        entity.setUnitPrice(new BigDecimal("10.00"));
        entity.setSubtotal(new BigDecimal("20.00"));

        OrderItem orderItem = entity.toDomain();

        assertEquals(1L, orderItem.getId());
        assertEquals("SKU-101", orderItem.getSku());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(0, new BigDecimal("10.00").compareTo(orderItem.getUnitPrice()));
        assertEquals(0, new BigDecimal("20.00").compareTo(orderItem.getSubtotal()));
    }

    @Test
    void shouldSetAndGetAllProperties() {
        OrderItemEntity entity = new OrderItemEntity();

        entity.setId(1L);
        entity.setSku("SKU-101");
        entity.setQuantity(2);
        entity.setUnitPrice(new BigDecimal("10.00"));
        entity.setSubtotal(new BigDecimal("20.00"));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        entity.setOrder(orderEntity);

        assertEquals(1L, entity.getId());
        assertEquals("SKU-101", entity.getSku());
        assertEquals(2, entity.getQuantity());
        assertEquals(0, new BigDecimal("10.00").compareTo(entity.getUnitPrice()));
        assertEquals(0, new BigDecimal("20.00").compareTo(entity.getSubtotal()));
        assertSame(orderEntity, entity.getOrder());
    }

    @Test
    void shouldCreateEmptyEntityWithNoArgsConstructor() {
        OrderItemEntity entity = new OrderItemEntity();

        assertNull(entity.getId());
        assertNull(entity.getSku());
        assertNull(entity.getQuantity());
        assertNull(entity.getUnitPrice());
        assertNull(entity.getSubtotal());
        assertNull(entity.getOrder());
    }

    @Test
    void shouldConvertFromDomainWithNullId() {
        OrderItem orderItem = new OrderItem(null, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"));

        OrderItemEntity entity = new OrderItemEntity(orderItem);

        assertNull(entity.getId());
        assertEquals("SKU-101", entity.getSku());
        assertEquals(2, entity.getQuantity());
    }

    @Test
    void shouldHandleZeroQuantity() {
        OrderItem orderItem = new OrderItem(1L, "SKU-101", 0, new BigDecimal("10.00"), BigDecimal.ZERO);

        OrderItemEntity entity = new OrderItemEntity(orderItem);

        assertEquals(0, entity.getQuantity());
        assertEquals(0, BigDecimal.ZERO.compareTo(entity.getSubtotal()));
    }

    @Test
    void shouldPreserveBigDecimalPrecision() {
        BigDecimal unitPrice = new BigDecimal("10.505");
        BigDecimal subtotal = new BigDecimal("31.515");
        OrderItem orderItem = new OrderItem(1L, "SKU-101", 3, unitPrice, subtotal);

        OrderItemEntity entity = new OrderItemEntity(orderItem);
        OrderItem convertedBack = entity.toDomain();

        assertEquals(0, unitPrice.compareTo(entity.getUnitPrice()));
        assertEquals(0, subtotal.compareTo(entity.getSubtotal()));
        assertEquals(0, unitPrice.compareTo(convertedBack.getUnitPrice()));
        assertEquals(0, subtotal.compareTo(convertedBack.getSubtotal()));
    }
}