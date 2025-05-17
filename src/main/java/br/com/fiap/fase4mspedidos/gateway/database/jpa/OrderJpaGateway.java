package br.com.fiap.fase4mspedidos.gateway.database.jpa;

import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.gateway.OrderGateway;
import br.com.fiap.fase4mspedidos.gateway.database.jpa.entity.OrderEntity;
import br.com.fiap.fase4mspedidos.gateway.database.jpa.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderJpaGateway implements
        OrderGateway {
    private final OrderRepository orderRepository;

    public OrderJpaGateway(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        OrderEntity savedEntity = orderRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id)
                .map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }
}