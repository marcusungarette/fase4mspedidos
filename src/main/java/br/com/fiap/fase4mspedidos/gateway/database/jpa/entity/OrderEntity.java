package br.com.fiap.fase4mspedidos.gateway.database.jpa.entity;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import br.com.fiap.fase4mspedidos.domain.entity.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Column(name = "credit_card_number", nullable = false)
    private String creditCardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (items != null) {
            items.forEach(item -> item.setOrder(this));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItemEntity item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    public OrderEntity(Order order) {
        this.id = order.getId();
        this.customerId = order.getCustomerId();
        this.creditCardNumber = order.getCreditCardNumber();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        if (order.getItems() != null) {
            this.items = new ArrayList<>();
            order.getItems().forEach(item -> {
                OrderItemEntity itemEntity = new OrderItemEntity(item);
                this.addItem(itemEntity);
            });
        }
    }

    public static OrderEntity fromDomain(Order order) {
        return new OrderEntity(order);
    }

    public Order toDomain() {
        List<OrderItem> orderItems = this.items.stream()
                .map(OrderItemEntity::toDomain)
                .collect(Collectors.toList());

        return new Order(
                this.id,
                this.customerId,
                orderItems,
                this.creditCardNumber,
                this.status,
                this.totalAmount,
                this.createdAt,
                this.updatedAt
        );
    }
}