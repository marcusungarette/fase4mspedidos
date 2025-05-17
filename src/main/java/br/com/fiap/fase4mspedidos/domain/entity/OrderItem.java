package br.com.fiap.fase4mspedidos.domain.entity;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItem {
    private final Long id;
    private final String sku;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItem(String sku, Integer quantity, BigDecimal unitPrice) {
        this(null, sku, quantity, unitPrice, unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}