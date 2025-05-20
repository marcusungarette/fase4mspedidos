package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.client.InventoryClient;
import br.com.fiap.fase4mspedidos.client.PaymentClient.PaymentNotification;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessPaymentCallbackUsecase {
    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentCallbackUsecase.class);

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
        logger.info("Processando notificação de pagamento: orderId={}, status={}",
                notification.getOrderId(), notification.getStatus());

        Long orderId = Long.parseLong(notification.getOrderId());
        Order order = orderGateway.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId.toString()));

        OrderStatus newStatus;
        switch (notification.getStatus()) {
            case "APPROVED":
                logger.info("Pagamento aprovado para o pedido {}", orderId);
                newStatus = OrderStatus.FECHADO_COM_SUCESSO;
                break;
            case "REJECTED":
                logger.info("Pagamento rejeitado para o pedido {}, restaurando estoque", orderId);
                newStatus = OrderStatus.FECHADO_SEM_CREDITO;
                restoreStockFromOperations(orderId);
                break;
            case "REFUNDED":
                logger.info("Pagamento estornado para o pedido {}, restaurando estoque", orderId);
                newStatus = OrderStatus.FECHADO_SEM_ESTOQUE;
                restoreStockFromOperations(orderId);
                break;
            default:
                logger.warn("Status de pagamento desconhecido: {}", notification.getStatus());
                return;
        }

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
        logger.info("Pedido {} atualizado com status {}", orderId, newStatus);
    }

    private void restoreStockFromOperations(Long orderId) {
        Map<Long, Integer> operations = orderStockOperations.get(orderId);

        if (operations == null || operations.isEmpty()) {
            logger.warn("Nenhuma operação de estoque encontrada para o pedido {}", orderId);
            return;
        }

        logger.info("Restaurando estoque para {} produtos do pedido {}", operations.size(), orderId);

        for (Map.Entry<Long, Integer> entry : operations.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            logger.info("Restaurando {} unidades do produto {} para o pedido {}",
                    quantity, productId, orderId);

            inventoryClient.restoreStock(productId, quantity);
        }
    }

    public void registerStockOperations(Long orderId, Map<Long, Integer> operations) {
        if (operations == null || operations.isEmpty()) {
            logger.info("Nenhuma operação de estoque para registrar para o pedido {}", orderId);
            return;
        }

        logger.info("Registrando {} operações de estoque para o pedido {}", operations.size(), orderId);
        orderStockOperations.put(orderId, new HashMap<>(operations));
    }
}