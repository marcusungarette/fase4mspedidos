package br.com.fiap.fase4mspedidos.gateway;

import br.com.fiap.fase4mspedidos.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderGateway {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findAll();
    void deleteById(Long id);
}