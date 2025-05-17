package br.com.fiap.fase4mspedidos.domain.entity;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class Order {
    private final Long id;
    private final Long customerId;
    private final List<OrderItem> items;
    private final String creditCardNumber;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}