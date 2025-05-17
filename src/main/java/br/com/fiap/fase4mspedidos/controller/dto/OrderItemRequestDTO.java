package br.com.fiap.fase4mspedidos.controller.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDTO {
    private String sku;
    private Integer quantity;
}