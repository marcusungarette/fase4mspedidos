package br.com.fiap.fase4mspedidos.controller;

import br.com.fiap.fase4mspedidos.controller.dto.OrderRequestDTO;
import br.com.fiap.fase4mspedidos.controller.dto.OrderResponseDTO;
import br.com.fiap.fase4mspedidos.controller.mapper.OrderMapper;
import br.com.fiap.fase4mspedidos.domain.entity.Order;
import br.com.fiap.fase4mspedidos.usecase.CreateOrderUsecase;
import br.com.fiap.fase4mspedidos.usecase.FindOrderUsecase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final CreateOrderUsecase createOrderUsecase;
    private final FindOrderUsecase findOrderUsecase;
    private final OrderMapper orderMapper;

    public OrderController(
            CreateOrderUsecase createOrderUsecase,
            FindOrderUsecase findOrderUsecase,
            OrderMapper orderMapper) {
        this.createOrderUsecase = createOrderUsecase;
        this.findOrderUsecase = findOrderUsecase;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO requestDTO) {
        Order createdOrder = createOrderUsecase.execute(requestDTO);
        OrderResponseDTO responseDTO = orderMapper.toResponseDTO(createdOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        Order order = findOrderUsecase.findById(id);
        OrderResponseDTO responseDTO = orderMapper.toResponseDTO(order);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = findOrderUsecase.findAll();
        List<OrderResponseDTO> responseDTOs = orders.stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }
}