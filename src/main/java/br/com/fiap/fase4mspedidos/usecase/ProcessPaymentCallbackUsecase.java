package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.client.InventoryClient;
import br.com.fiap.fase4mspedidos.client.PaymentClient.PaymentNotification;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessPaymentCallbackUsecase {

    private final OrderGateway orderGateway;
    private final InventoryClient inventoryClient;
    private final Map<Long, Map<Long, Integer>> orderStockOperations = new HashMap<>();

    public ProcessPaymentCallbackUsecase(
            OrderGateway orderGateway,
            InventoryClient inventoryClient) {
        this.orderGateway = orderGateway;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public void execute(PaymentNotification notification) {
        Long orderId = Long.parseLong(notification.getOrderId());
        Order order = orderGateway.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        OrderStatus newStatus;
        switch (notification.getStatus()) {
            case "APPROVED":
                newStatus = OrderStatus.FECHADO_COM_SUCESSO;
                break;
            case "REJECTED":
                newStatus = OrderStatus.FECHADO_SEM_CREDITO;
                restoreStock(order);
                break;
            case "REFUNDED":
                newStatus = OrderStatus.FECHADO_SEM_ESTOQUE;
                restoreStock(order);
                break;
            default:
                return;
        }

        // Atualizar o pedido
        Order updatedOrder = new Order(
                order.getId(),
                order.getCustomerId(),
                order.getItems(),
                order.getCreditCardNumber(),
                newStatus,
                order.getTotalAmount(),
                order.getCreatedAt(),
                LocalDateTime.now()
        );
        orderGateway.save(updatedOrder);
    }

    // Este metodo restauraria o estoque para cada item do pedido
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Long productId = getProductIdFromSku(item.getSku());
            if (productId != null) {
                inventoryClient.restoreStock(productId, item.getQuantity());
            }
        }
    }
    public Long getProductIdFromSku(String sku) {
        return null;
    }
    // Metodo para registrar operações de estoque
    public void registerStockOperations(Long orderId, Map<Long, Integer> operations) {
        orderStockOperations.put(orderId, operations);
    }
}