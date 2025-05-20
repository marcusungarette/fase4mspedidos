package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.client.InventoryClient;
import br.com.fiap.fase4mspedidos.client.PaymentClient.PaymentNotification;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessPaymentCallbackUsecaseTest {

    @Mock
    private OrderGateway orderGateway;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private ProcessPaymentCallbackUsecase usecase;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<Long> productIdCaptor;

    @Captor
    private ArgumentCaptor<Integer> quantityCaptor;

    private Order mockOrder;
    private final LocalDateTime now = LocalDateTime.now();
    private final long orderId = 123L;

    @BeforeEach
    void setUp() {
        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("312.32"), new BigDecimal("100.00")),
                new OrderItem(2L, "SKU-102", 1, new BigDecimal("312.32"), new BigDecimal("50.00"))
        );

        mockOrder = new Order(
                orderId,
                456L,
                items,
                "4111111111111111",
                OrderStatus.ABERTO,
                new BigDecimal("250.00"),
                now.minusHours(1),
                now.minusHours(1)
        );

        when(orderGateway.findById(orderId)).thenReturn(Optional.of(mockOrder));
    }

    @Test
    void execute_ShouldUpdateOrderStatus_WhenPaymentApproved() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn(String.valueOf(orderId));
        when(notificationMock.getStatus()).thenReturn("APPROVED");

        usecase.execute(notificationMock);

        verify(orderGateway).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.FECHADO_COM_SUCESSO, savedOrder.getStatus());
        verify(inventoryClient, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void execute_ShouldUpdateOrderStatusAndRestoreStock_WhenPaymentRejected() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn(String.valueOf(orderId));
        when(notificationMock.getStatus()).thenReturn("REJECTED");

        Map<Long, Integer> stockOperations = new HashMap<>();
        stockOperations.put(101L, 2);
        stockOperations.put(102L, 1);
        usecase.registerStockOperations(orderId, stockOperations);

        usecase.execute(notificationMock);

        verify(orderGateway).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.FECHADO_SEM_CREDITO, savedOrder.getStatus());

        verify(inventoryClient, times(2)).restoreStock(productIdCaptor.capture(), quantityCaptor.capture());
        List<Long> capturedProductIds = productIdCaptor.getAllValues();
        List<Integer> capturedQuantities = quantityCaptor.getAllValues();

        assertTrue(capturedProductIds.contains(101L));
        assertTrue(capturedProductIds.contains(102L));
        assertTrue(capturedQuantities.contains(2));
        assertTrue(capturedQuantities.contains(1));
    }

    @Test
    void execute_ShouldUpdateOrderStatusAndRestoreStock_WhenPaymentRefunded() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn(String.valueOf(orderId));
        when(notificationMock.getStatus()).thenReturn("REFUNDED");

        Map<Long, Integer> stockOperations = new HashMap<>();
        stockOperations.put(101L, 2);
        stockOperations.put(102L, 1);
        usecase.registerStockOperations(orderId, stockOperations);

        usecase.execute(notificationMock);

        verify(orderGateway).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.FECHADO_SEM_ESTOQUE, savedOrder.getStatus());

        verify(inventoryClient, times(2)).restoreStock(anyLong(), anyInt());
    }

    @Test
    void execute_ShouldDoNothing_WhenStatusUnknown() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn(String.valueOf(orderId));
        when(notificationMock.getStatus()).thenReturn("UNKNOWN_STATUS");

        usecase.execute(notificationMock);

        verify(orderGateway, never()).save(any());
        verify(inventoryClient, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void execute_ShouldThrowException_WhenOrderNotFound() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn("999");
        when(orderGateway.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> usecase.execute(notificationMock));
    }

    @Test
    void execute_ShouldNotRestoreStock_WhenNoStockOperationsRegistered() {
        PaymentNotification notificationMock = mock(PaymentNotification.class);
        when(notificationMock.getOrderId()).thenReturn(String.valueOf(orderId));
        when(notificationMock.getStatus()).thenReturn("REJECTED");

        usecase.execute(notificationMock);

        verify(orderGateway).save(any(Order.class));
        verify(inventoryClient, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void restoreStockFromOperations_ShouldRestoreStock_WhenOperationsExist() throws Exception {
        Method restoreStockMethod = ProcessPaymentCallbackUsecase.class.getDeclaredMethod(
                "restoreStockFromOperations", Long.class);
        restoreStockMethod.setAccessible(true);

        Map<Long, Integer> operations = new HashMap<>();
        operations.put(101L, 2);
        operations.put(102L, 1);
        usecase.registerStockOperations(orderId, operations);

        restoreStockMethod.invoke(usecase, orderId);

        verify(inventoryClient, times(2)).restoreStock(productIdCaptor.capture(), quantityCaptor.capture());

        List<Long> capturedProductIds = productIdCaptor.getAllValues();
        List<Integer> capturedQuantities = quantityCaptor.getAllValues();

        assertTrue(capturedProductIds.contains(101L));
        assertTrue(capturedProductIds.contains(102L));
        assertTrue(capturedQuantities.contains(2));
        assertTrue(capturedQuantities.contains(1));
    }

    @Test
    void restoreStockFromOperations_ShouldDoNothing_WhenNoOperationsExist() throws Exception {
        Method restoreStockMethod = ProcessPaymentCallbackUsecase.class.getDeclaredMethod(
                "restoreStockFromOperations", Long.class);
        restoreStockMethod.setAccessible(true);

        restoreStockMethod.invoke(usecase, orderId);

        verify(inventoryClient, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void registerStockOperations_ShouldStoreOperationsMap() {
        Long orderId = 123L;
        Map<Long, Integer> operations = new HashMap<>();
        operations.put(101L, 2);
        operations.put(102L, 1);

        usecase.registerStockOperations(orderId, operations);

        try {
            java.lang.reflect.Field field = ProcessPaymentCallbackUsecase.class.getDeclaredField("orderStockOperations");
            field.setAccessible(true);
            Map<Long, Map<Long, Integer>> orderStockOps = (Map<Long, Map<Long, Integer>>) field.get(usecase);

            assertTrue(orderStockOps.containsKey(orderId));
            assertEquals(operations.size(), orderStockOps.get(orderId).size());
            assertEquals(operations.get(101L), orderStockOps.get(orderId).get(101L));
            assertEquals(operations.get(102L), orderStockOps.get(orderId).get(102L));
        } catch (Exception e) {
            fail("Failed to access orderStockOperations field: " + e.getMessage());
        }
    }

    @Test
    void registerStockOperations_ShouldOverwriteExistingOperations() {
        Long orderId = 123L;
        Map<Long, Integer> operations1 = new HashMap<>();
        operations1.put(101L, 2);

        Map<Long, Integer> operations2 = new HashMap<>();
        operations2.put(102L, 3);

        usecase.registerStockOperations(orderId, operations1);
        usecase.registerStockOperations(orderId, operations2);

        try {
            java.lang.reflect.Field field = ProcessPaymentCallbackUsecase.class.getDeclaredField("orderStockOperations");
            field.setAccessible(true);
            Map<Long, Map<Long, Integer>> orderStockOps = (Map<Long, Map<Long, Integer>>) field.get(usecase);

            assertTrue(orderStockOps.containsKey(orderId));
            assertEquals(operations2.size(), orderStockOps.get(orderId).size());
            assertEquals(operations2.get(102L), orderStockOps.get(orderId).get(102L));
            assertFalse(orderStockOps.get(orderId).containsKey(101L)); // Deve ter sido sobrescrito
        } catch (Exception e) {
            fail("Failed to access orderStockOperations field: " + e.getMessage());
        }
    }

    @Test
    void registerStockOperations_ShouldNotRegister_WhenOperationsEmpty() {
        Long orderId = 456L;
        Map<Long, Integer> emptyOperations = new HashMap<>();

        usecase.registerStockOperations(orderId, emptyOperations);

        try {
            java.lang.reflect.Field field = ProcessPaymentCallbackUsecase.class.getDeclaredField("orderStockOperations");
            field.setAccessible(true);
            Map<Long, Map<Long, Integer>> orderStockOps = (Map<Long, Map<Long, Integer>>) field.get(usecase);

            assertFalse(orderStockOps.containsKey(orderId));
        } catch (Exception e) {
            fail("Failed to access orderStockOperations field: " + e.getMessage());
        }
    }

    @Test
    void registerStockOperations_ShouldNotRegister_WhenOperationsNull() {
        Long orderId = 456L;

        usecase.registerStockOperations(orderId, null);

        try {
            java.lang.reflect.Field field = ProcessPaymentCallbackUsecase.class.getDeclaredField("orderStockOperations");
            field.setAccessible(true);
            Map<Long, Map<Long, Integer>> orderStockOps = (Map<Long, Map<Long, Integer>>) field.get(usecase);

            assertFalse(orderStockOps.containsKey(orderId));
        } catch (Exception e) {
            fail("Failed to access orderStockOperations field: " + e.getMessage());
        }
    }
}