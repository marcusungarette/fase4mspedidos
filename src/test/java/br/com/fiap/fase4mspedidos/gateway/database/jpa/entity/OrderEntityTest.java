package br.com.fiap.fase4mspedidos.gateway.database.jpa.entity;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderEntityTest {

    @Test
    void constructor_ShouldCreateEmptyEntity_WhenNoArgsProvided() {
        // Act
        OrderEntity entity = new OrderEntity();

        // Assert
        assertNull(entity.getId());
        assertNull(entity.getCustomerId());
        assertNull(entity.getCreditCardNumber());
        assertNull(entity.getStatus());
        assertNull(entity.getTotalAmount());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNotNull(entity.getItems());
        assertTrue(entity.getItems().isEmpty());
    }

    @Test
    void constructor_ShouldMapFromDomain_WhenOrderProvided() {
        // Arrange
        Long id = 1L;
        Long customerId = 100L;
        String creditCardNumber = "4111111111111111";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("150.00");
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        List<OrderItem> orderItems = Arrays.asList(
                new OrderItem(1L, "SKU-001", 2, new BigDecimal("50.00"), new BigDecimal("100.00")),
                new OrderItem(2L, "SKU-002", 1, new BigDecimal("50.00"), new BigDecimal("50.00"))
        );

        Order order = new Order(id, customerId, orderItems, creditCardNumber, status, totalAmount, createdAt, updatedAt);

        // Act
        OrderEntity entity = new OrderEntity(order);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(creditCardNumber, entity.getCreditCardNumber());
        assertEquals(status, entity.getStatus());
        assertEquals(totalAmount, entity.getTotalAmount());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
        assertEquals(2, entity.getItems().size());
        assertEquals("SKU-001", entity.getItems().get(0).getSku());
        assertEquals("SKU-002", entity.getItems().get(1).getSku());
    }

    @Test
    void constructor_ShouldHandleNullItems_WhenOrderItemsAreNull() {
        // Arrange
        Order order = new Order(1L, 100L, null, "4111111111111111",
                OrderStatus.ABERTO, new BigDecimal("150.00"),
                LocalDateTime.now(), LocalDateTime.now());

        // Act
        OrderEntity entity = new OrderEntity(order);

        // Assert
        assertNotNull(entity.getItems());
        assertTrue(entity.getItems().isEmpty());
    }

    @Test
    void fromDomain_ShouldCreateEntity() {
        // Arrange
        Order order = new Order(1L, 100L, Collections.emptyList(), "4111111111111111",
                OrderStatus.ABERTO, new BigDecimal("150.00"),
                LocalDateTime.now(), LocalDateTime.now());

        // Act
        OrderEntity entity = OrderEntity.fromDomain(order);

        // Assert
        assertEquals(order.getId(), entity.getId());
        assertEquals(order.getCustomerId(), entity.getCustomerId());
        assertEquals(order.getStatus(), entity.getStatus());
    }

    @Test
    void toDomain_ShouldCreateDomainObject() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setCustomerId(100L);
        entity.setCreditCardNumber("4111111111111111");
        entity.setStatus(OrderStatus.ABERTO);
        entity.setTotalAmount(new BigDecimal("150.00"));
        entity.setCreatedAt(LocalDateTime.now().minusHours(1));
        entity.setUpdatedAt(LocalDateTime.now());

        OrderItemEntity item1 = new OrderItemEntity();
        item1.setId(1L);
        item1.setSku("SKU-001");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setSubtotal(new BigDecimal("100.00"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setId(2L);
        item2.setSku("SKU-002");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("50.00"));
        item2.setSubtotal(new BigDecimal("50.00"));

        entity.addItem(item1);
        entity.addItem(item2);

        // Act
        Order order = entity.toDomain();

        // Assert
        assertEquals(entity.getId(), order.getId());
        assertEquals(entity.getCustomerId(), order.getCustomerId());
        assertEquals(entity.getCreditCardNumber(), order.getCreditCardNumber());
        assertEquals(entity.getStatus(), order.getStatus());
        assertEquals(entity.getTotalAmount(), order.getTotalAmount());
        assertEquals(entity.getCreatedAt(), order.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), order.getUpdatedAt());
        assertEquals(2, order.getItems().size());
    }

    @Test
    void addItem_ShouldAddItemAndSetOrder() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        OrderItemEntity item = new OrderItemEntity();

        // Act
        entity.addItem(item);

        // Assert
        assertEquals(1, entity.getItems().size());
        assertTrue(entity.getItems().contains(item));
        assertEquals(entity, item.getOrder());
    }

    @Test
    void addItem_ShouldInitializeItemsIfNull() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        entity.setItems(null); // Force items to be null
        OrderItemEntity item = new OrderItemEntity();

        // Act
        entity.addItem(item);

        // Assert
        assertNotNull(entity.getItems());
        assertEquals(1, entity.getItems().size());
    }

    @Test
    void onCreate_ShouldSetDatesAndItemRelationships() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        OrderItemEntity item = new OrderItemEntity();
        entity.addItem(item);

        // Act
        entity.onCreate();

        // Assert
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(entity, entity.getItems().get(0).getOrder());
    }

    @Test
    void onCreate_ShouldHandleNullItems() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        entity.setItems(null);

        // Act - Should not throw exception
        entity.onCreate();

        // Assert
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        LocalDateTime before = LocalDateTime.now();

        // Act
        entity.onUpdate();

        // Assert
        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(before) || entity.getUpdatedAt().isEqual(before));
    }

    @Test
    void setters_ShouldSetAllFields() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        Long id = 1L;
        Long customerId = 100L;
        List<OrderItemEntity> items = new ArrayList<>();
        String creditCardNumber = "4111111111111111";
        OrderStatus status = OrderStatus.ABERTO;
        BigDecimal totalAmount = new BigDecimal("150.00");
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        // Act
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setItems(items);
        entity.setCreditCardNumber(creditCardNumber);
        entity.setStatus(status);
        entity.setTotalAmount(totalAmount);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(items, entity.getItems());
        assertEquals(creditCardNumber, entity.getCreditCardNumber());
        assertEquals(status, entity.getStatus());
        assertEquals(totalAmount, entity.getTotalAmount());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
}