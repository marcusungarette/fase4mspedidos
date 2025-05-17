package br.com.fiap.fase4mspedidos.gateway.database.jpa.entity;

import br.com.fiap.fase4mspedidos.domain.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    public OrderItemEntity(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.sku = orderItem.getSku();
        this.quantity = orderItem.getQuantity();
        this.unitPrice = orderItem.getUnitPrice();
        this.subtotal = orderItem.getSubtotal();
    }

    public OrderItem toDomain() {
        return new OrderItem(
                this.id,
                this.sku,
                this.quantity,
                this.unitPrice,
                this.subtotal
        );
    }
}