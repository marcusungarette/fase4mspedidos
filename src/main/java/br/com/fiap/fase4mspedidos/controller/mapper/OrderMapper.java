package br.com.fiap.fase4mspedidos.controller.mapper;

import br.com.fiap.fase4mspedidos.controller.dto.OrderItemRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderItemResponseDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderResponseDTO;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toDomain(OrderRequestDTO dto, List<OrderItem> items, BigDecimal totalAmount) {
        return new Order(
                null,
                dto.getCustomerId(),
                items,
                dto.getCreditCardNumber(),
                OrderStatus.ABERTO,
                totalAmount,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public OrderItem toOrderItem(OrderItemRequestDTO dto, BigDecimal unitPrice) {
        return new OrderItem(dto.getSku(), dto.getQuantity(), unitPrice);
    }

    public OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerId(),
                items,
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem) {
        return new OrderItemResponseDTO(
                orderItem.getId(),
                orderItem.getSku(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getSubtotal()
        );
    }
}