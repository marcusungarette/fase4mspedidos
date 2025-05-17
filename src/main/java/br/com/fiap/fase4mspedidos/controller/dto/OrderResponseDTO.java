package br.com.fiap.fase4mspedidos.controller.dto;

import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long customerId;
    private List<OrderItemResponseDTO> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}