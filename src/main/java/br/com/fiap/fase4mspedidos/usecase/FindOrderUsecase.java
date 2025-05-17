package br.com.fiap.fase4mspedidos.usecase;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.domain.exception.OrderNotFoundException;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindOrderUsecase {
    private final OrderGateway orderGateway;

    public FindOrderUsecase(OrderGateway orderGateway) {
        this.orderGateway = orderGateway;
    }

    public Order findById(Long id) {
        return orderGateway.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id.toString()));
    }

    public List<Order> findAll() {
        return orderGateway.findAll();
    }
}