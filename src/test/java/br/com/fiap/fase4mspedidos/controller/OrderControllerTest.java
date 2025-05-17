package br.com.fiap.fase4mspedidos.controller;

import br.com.fiap.fase4mspedidos.config.GlobalExceptionHandler;
import br.com.fiap.fase4mspedidos.controller.dto.OrderItemResponseDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderResponseDTO;
import br.com.fiap.fase4mspedidos.controller.mapper.OrderMapper;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.domain.exception.ProductNotFoundException;
import br.com.fiap.fase4mspedidos.usecase.CreateOrderUsecase;
import br.com.fiap.fase4mspedidos.usecase.FindOrderUsecase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private CreateOrderUsecase createOrderUsecase;

    @Mock
    private FindOrderUsecase findOrderUsecase;

    @Mock
    private OrderMapper orderMapper;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(
                createOrderUsecase,
                findOrderUsecase,
                orderMapper
        );

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO(
                100L,
                Arrays.asList(),
                "1234-5678-9012-3456"
        );

        LocalDateTime now = LocalDateTime.now();

        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        Order createdOrder = new Order(
                1L,
                100L,
                items,
                "1234-5678-9012-3456",
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        List<OrderItemResponseDTO> itemResponseDTOs = Arrays.asList(
                new OrderItemResponseDTO(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                1L,
                100L,
                itemResponseDTOs,
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        when(createOrderUsecase.execute(any(OrderRequestDTO.class))).thenReturn(createdOrder);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(100)))
                .andExpect(jsonPath("$.status", is("ABERTO")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].sku", is("SKU-101")));

        verify(createOrderUsecase).execute(any(OrderRequestDTO.class));
        verify(orderMapper).toResponseDTO(any(Order.class));
    }

    @Test
    void shouldGetOrderByIdSuccessfully() throws Exception {
        Long orderId = 1L;

        LocalDateTime now = LocalDateTime.now();

        List<OrderItem> items = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        Order order = new Order(
                orderId,
                100L,
                items,
                "1234-5678-9012-3456",
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        List<OrderItemResponseDTO> itemResponseDTOs = Arrays.asList(
                new OrderItemResponseDTO(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                orderId,
                100L,
                itemResponseDTOs,
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        when(findOrderUsecase.findById(orderId)).thenReturn(order);
        when(orderMapper.toResponseDTO(order)).thenReturn(responseDTO);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(100)))
                .andExpect(jsonPath("$.status", is("ABERTO")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].sku", is("SKU-101")));

        verify(findOrderUsecase).findById(orderId);
        verify(orderMapper).toResponseDTO(order);
    }

    @Test
    void shouldGetAllOrdersSuccessfully() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        List<OrderItem> items1 = Arrays.asList(
                new OrderItem(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        List<OrderItem> items2 = Arrays.asList(
                new OrderItem(2L, "SKU-102", 1, new BigDecimal("15.00"), new BigDecimal("15.00"))
        );

        Order order1 = new Order(
                1L,
                100L,
                items1,
                "1234-5678-9012-3456",
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        Order order2 = new Order(
                2L,
                200L,
                items2,
                "9876-5432-1098-7654",
                OrderStatus.FECHADO_COM_SUCESSO,
                new BigDecimal("15.00"),
                now,
                now
        );

        List<Order> orders = Arrays.asList(order1, order2);

        List<OrderItemResponseDTO> itemResponseDTOs1 = Arrays.asList(
                new OrderItemResponseDTO(1L, "SKU-101", 2, new BigDecimal("10.00"), new BigDecimal("20.00"))
        );

        List<OrderItemResponseDTO> itemResponseDTOs2 = Arrays.asList(
                new OrderItemResponseDTO(2L, "SKU-102", 1, new BigDecimal("15.00"), new BigDecimal("15.00"))
        );

        OrderResponseDTO responseDTO1 = new OrderResponseDTO(
                1L,
                100L,
                itemResponseDTOs1,
                OrderStatus.ABERTO,
                new BigDecimal("20.00"),
                now,
                now
        );

        OrderResponseDTO responseDTO2 = new OrderResponseDTO(
                2L,
                200L,
                itemResponseDTOs2,
                OrderStatus.FECHADO_COM_SUCESSO,
                new BigDecimal("15.00"),
                now,
                now
        );

        when(findOrderUsecase.findAll()).thenReturn(orders);
        when(orderMapper.toResponseDTO(order1)).thenReturn(responseDTO1);
        when(orderMapper.toResponseDTO(order2)).thenReturn(responseDTO2);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].customerId", is(100)))
                .andExpect(jsonPath("$[0].status", is("ABERTO")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].customerId", is(200)))
                .andExpect(jsonPath("$[1].status", is("FECHADO_COM_SUCESSO")));

        verify(findOrderUsecase).findAll();
        verify(orderMapper).toResponseDTO(order1);
        verify(orderMapper).toResponseDTO(order2);
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() throws Exception {
        when(findOrderUsecase.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(findOrderUsecase).findAll();
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        Long orderId = 999L;

        when(findOrderUsecase.findById(orderId))
                .thenThrow(new OrderNotFoundException(orderId.toString()));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Order not found with id: " + orderId)));

        verify(findOrderUsecase).findById(orderId);
        verify(orderMapper, never()).toResponseDTO(any(Order.class));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO(
                100L,
                Arrays.asList(),
                "1234-5678-9012-3456"
        );

        String nonExistentSku = "NON-EXISTENT-SKU";
        when(createOrderUsecase.execute(any(OrderRequestDTO.class)))
                .thenThrow(new ProductNotFoundException(nonExistentSku));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Product not found with SKU: " + nonExistentSku)));

        verify(createOrderUsecase).execute(any(OrderRequestDTO.class));
        verify(orderMapper, never()).toResponseDTO(any(Order.class));
    }
}