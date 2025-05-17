package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.client.CustomerClient;
import br.com.fiap.fase4mspedidos.client.InventoryClient;
import br.com.fiap.fase4mspedidos.client.PaymentClient;
import br.com.fiap.fase4mspedidos.client.ProductClient;
import br.com.fiap.fase4mspedidos.controller.dto.OrderItemRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderRequestDTO;
import br.com.fiap.fase4mspedidos.controller.mapper.OrderMapper;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateOrderUsecaseTest {

    @Mock
    private OrderGateway orderGateway;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductClient productClient;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private ProcessPaymentCallbackUsecase processPaymentCallbackUsecase;

    @InjectMocks
    private CreateOrderUsecase usecase;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<Map<Long, Integer>> stockOperationsCaptor;

    @Test
    void execute_ShouldCreateOrder_WhenEverythingIsValid() {
        // Configurar DTOs
        OrderItemRequestDTO item1 = mock(OrderItemRequestDTO.class);
        when(item1.getSku()).thenReturn("SKU-001");
        when(item1.getQuantity()).thenReturn(2);

        OrderItemRequestDTO item2 = mock(OrderItemRequestDTO.class);
        when(item2.getSku()).thenReturn("SKU-002");
        when(item2.getQuantity()).thenReturn(1);

        OrderRequestDTO orderRequest = mock(OrderRequestDTO.class);
        when(orderRequest.getCustomerId()).thenReturn(1L);
        when(orderRequest.getCreditCardNumber()).thenReturn("4111111111111111");
        when(orderRequest.getItems()).thenReturn(Arrays.asList(item1, item2));

        // Configurar OrderItems
        List<OrderItem> mockOrderItems = Arrays.asList(
                new OrderItem(null, "SKU-001", 2, new BigDecimal("100.00"), new BigDecimal("200.00")),
                new OrderItem(null, "SKU-002", 1, new BigDecimal("50.00"), new BigDecimal("50.00"))
        );

        // Configurar Order
        Order mockOrder = new Order(
                1L,
                1L,
                mockOrderItems,
                "4111111111111111",
                OrderStatus.ABERTO,
                new BigDecimal("250.00"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Configurar ProductResponse
        ProductClient.ProductResponse mockProduct1 = mock(ProductClient.ProductResponse.class);
        when(mockProduct1.getId()).thenReturn(101L);
        when(mockProduct1.getPrice()).thenReturn(new BigDecimal("100.00"));

        ProductClient.ProductResponse mockProduct2 = mock(ProductClient.ProductResponse.class);
        when(mockProduct2.getId()).thenReturn(102L);
        when(mockProduct2.getPrice()).thenReturn(new BigDecimal("50.00"));

        // Configurar PaymentResponse
        PaymentClient.PaymentResponse mockPaymentResponse = mock(PaymentClient.PaymentResponse.class);

        // Configurar mocks
        when(customerClient.validateCustomer(1L)).thenReturn(true);
        when(productClient.getProductBySku("SKU-001")).thenReturn(mockProduct1);
        when(productClient.getProductBySku("SKU-002")).thenReturn(mockProduct2);
        when(inventoryClient.checkAvailability(eq(101L), eq(2))).thenReturn(true);
        when(inventoryClient.checkAvailability(eq(102L), eq(1))).thenReturn(true);
        when(orderMapper.toOrderItem(any(OrderItemRequestDTO.class), any(BigDecimal.class)))
                .thenReturn(mockOrderItems.get(0))
                .thenReturn(mockOrderItems.get(1));
        when(orderMapper.toDomain(eq(orderRequest), anyList(), any(BigDecimal.class))).thenReturn(mockOrder);
        when(orderGateway.save(any(Order.class))).thenReturn(mockOrder);
        when(paymentClient.processPayment(any(PaymentClient.PaymentRequest.class))).thenReturn(mockPaymentResponse);

        // Executar
        Order result = usecase.execute(orderRequest);

        // Verificar
        assertEquals(mockOrder, result);
        verify(inventoryClient).reduceStock(eq(101L), eq(2));
        verify(inventoryClient).reduceStock(eq(102L), eq(1));
        verify(processPaymentCallbackUsecase).registerStockOperations(eq(1L), stockOperationsCaptor.capture());
        Map<Long, Integer> capturedStockOperations = stockOperationsCaptor.getValue();
        assertEquals(2, capturedStockOperations.get(101L));
        assertEquals(1, capturedStockOperations.get(102L));
        verify(paymentClient).processPayment(any(PaymentClient.PaymentRequest.class));
    }

    @Test
    void execute_ShouldThrowException_WhenCustomerIsInvalid() {
        // Configurar apenas o que é necessário para este teste
        OrderRequestDTO orderRequest = mock(OrderRequestDTO.class);
        when(orderRequest.getCustomerId()).thenReturn(1L);

        // Configurar mock
        when(customerClient.validateCustomer(1L)).thenReturn(false);

        // Executar e verificar
        assertThrows(RuntimeException.class, () -> usecase.execute(orderRequest));

        verify(productClient, never()).getProductBySku(anyString());
        verify(inventoryClient, never()).checkAvailability(anyLong(), anyInt());
        verify(orderGateway, never()).save(any(Order.class));
    }

    @Test
    void execute_ShouldHandleInsufficientStock_AndUpdateOrderStatus() {
        OrderItemRequestDTO item1 = mock(OrderItemRequestDTO.class);
        when(item1.getSku()).thenReturn("SKU-001");
        when(item1.getQuantity()).thenReturn(2);

        OrderItemRequestDTO item2 = mock(OrderItemRequestDTO.class);
        when(item2.getSku()).thenReturn("SKU-002");
        when(item2.getQuantity()).thenReturn(1);

        OrderRequestDTO orderRequest = mock(OrderRequestDTO.class);
        when(orderRequest.getCustomerId()).thenReturn(1L);
        when(orderRequest.getCreditCardNumber()).thenReturn("4111111111111111");
        when(orderRequest.getItems()).thenReturn(Arrays.asList(item1, item2));

        OrderItem orderItem1 = new OrderItem(null, "SKU-001", 2, new BigDecimal("100.00"), new BigDecimal("200.00"));
        OrderItem orderItem2 = new OrderItem(null, "SKU-002", 1, new BigDecimal("50.00"), new BigDecimal("50.00"));
        List<OrderItem> mockOrderItems = Arrays.asList(orderItem1, orderItem2);

        Order mockOrder = new Order(
                1L,
                1L,
                mockOrderItems,
                "4111111111111111",
                OrderStatus.ABERTO,
                new BigDecimal("250.00"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        ProductClient.ProductResponse mockProduct1 = mock(ProductClient.ProductResponse.class);
        when(mockProduct1.getId()).thenReturn(101L);
        when(mockProduct1.getPrice()).thenReturn(new BigDecimal("100.00"));

        ProductClient.ProductResponse mockProduct2 = mock(ProductClient.ProductResponse.class);
        when(mockProduct2.getId()).thenReturn(102L);
        when(mockProduct2.getPrice()).thenReturn(new BigDecimal("50.00"));

        // IMPORTANTE: Configurar de forma mais precisa o mapper para garantir que retorne os itens corretos
        when(orderMapper.toOrderItem(eq(item1), any(BigDecimal.class))).thenReturn(orderItem1);
        when(orderMapper.toOrderItem(eq(item2), any(BigDecimal.class))).thenReturn(orderItem2);

        when(customerClient.validateCustomer(1L)).thenReturn(true);
        when(productClient.getProductBySku("SKU-001")).thenReturn(mockProduct1);
        when(productClient.getProductBySku("SKU-002")).thenReturn(mockProduct2);
        when(inventoryClient.checkAvailability(eq(101L), eq(2))).thenReturn(true);
        when(inventoryClient.checkAvailability(eq(102L), eq(1))).thenReturn(false); // Um item sem estoque
        when(orderMapper.toDomain(eq(orderRequest), anyList(), any(BigDecimal.class))).thenReturn(mockOrder);
        when(orderGateway.save(any(Order.class))).thenReturn(mockOrder);

        Order result = usecase.execute(orderRequest);

        verify(inventoryClient).reduceStock(eq(101L), eq(2));
        verify(inventoryClient, never()).reduceStock(eq(102L), eq(1));
        verify(processPaymentCallbackUsecase).registerStockOperations(eq(1L), anyMap());
        verify(inventoryClient).restoreStock(eq(101L), eq(2));
        verify(orderGateway, times(2)).save(orderCaptor.capture());
        List<Order> savedOrders = orderCaptor.getAllValues();
        assertEquals(OrderStatus.FECHADO_SEM_ESTOQUE, savedOrders.get(1).getStatus());
        verify(paymentClient, never()).processPayment(any(PaymentClient.PaymentRequest.class));
    }
}