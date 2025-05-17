package br.com.fiap.fase4mspedidos.client;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public interface ProductClient {
    ProductResponse getProductBySku(String sku);

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    class ProductResponse {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal price;
    }
}