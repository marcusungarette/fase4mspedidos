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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateOrderUsecase {
    private final OrderGateway orderGateway;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final CustomerClient customerClient;
    private final ProcessPaymentCallbackUsecase processPaymentCallbackUsecase;

    public CreateOrderUsecase(
            OrderGateway orderGateway,
            OrderMapper orderMapper,
            ProductClient productClient,
            InventoryClient inventoryClient,
            PaymentClient paymentClient,
            CustomerClient customerClient,
            ProcessPaymentCallbackUsecase processPaymentCallbackUsecase) {
        this.orderGateway = orderGateway;
        this.orderMapper = orderMapper;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.customerClient = customerClient;
        this.processPaymentCallbackUsecase = processPaymentCallbackUsecase;
    }

    @Transactional
    public Order execute(OrderRequestDTO orderRequest) {
        if (!customerClient.validateCustomer(orderRequest.getCustomerId())) {
            throw new RuntimeException("Cliente inválido");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, Integer> stockOperations = new HashMap<>();
        boolean hasInsufficientStock = false;

        for (OrderItemRequestDTO itemRequest : orderRequest.getItems()) {
            ProductClient.ProductResponse product = productClient.getProductBySku(itemRequest.getSku());

            boolean isAvailable = inventoryClient.checkAvailability(product.getId(), itemRequest.getQuantity());

            if (!isAvailable) {
                hasInsufficientStock = true;
            }

            OrderItem orderItem = orderMapper.toOrderItem(itemRequest, product.getPrice());
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());

            if (isAvailable) {
                inventoryClient.reduceStock(product.getId(), itemRequest.getQuantity());
                stockOperations.put(product.getId(), itemRequest.getQuantity());
            }
        }

        // Criar pedido com status inicial ABERTO
        Order order = orderMapper.toDomain(orderRequest, orderItems, totalAmount);
        Order savedOrder = orderGateway.save(order);

        processPaymentCallbackUsecase.registerStockOperations(savedOrder.getId(), stockOperations);

        if (hasInsufficientStock) {
            for (Map.Entry<Long, Integer> entry : stockOperations.entrySet()) {
                inventoryClient.restoreStock(entry.getKey(), entry.getValue());
            }

            Order updatedOrder = new Order(
                    savedOrder.getId(),
                    savedOrder.getCustomerId(),
                    savedOrder.getItems(),
                    savedOrder.getCreditCardNumber(),
                    OrderStatus.FECHADO_SEM_ESTOQUE,
                    savedOrder.getTotalAmount(),
                    savedOrder.getCreatedAt(),
                    LocalDateTime.now()
            );
            return orderGateway.save(updatedOrder);
        }

        PaymentClient.PaymentRequest paymentRequest = new PaymentClient.PaymentRequest(
                orderRequest.getCreditCardNumber(),
                totalAmount,
                savedOrder.getId().toString(),
                null // callbackUrl será preenchido no PaymentClientImpl
        );

        PaymentClient.PaymentResponse paymentResponse = paymentClient.processPayment(paymentRequest);
        return savedOrder;
    }
}