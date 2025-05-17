package br.com.fiap.fase4mspedidos.controller.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
    private Long customerId;
    private List<OrderItemRequestDTO> items;
    private String creditCardNumber;
}