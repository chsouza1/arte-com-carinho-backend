package com.artecomcarinho.controller;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.model.Order.OrderStatus;
import com.artecomcarinho.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Orders", description = "Gerenciamento de Pedidos")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna lista paginada de pedidos")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @PageableDefault(size = 20, sort = "orderDate,desc") Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna um pedido específico pelo ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Buscar pedido por número", description = "Retorna um pedido específico pelo número")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Buscar pedidos por cliente", description = "Retorna pedidos de um cliente específico")
    public ResponseEntity<Page<OrderDTO>> getOrdersByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "orderDate,desc") Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar pedidos por status", description = "Retorna pedidos com status específico")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate,desc") Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Buscar pedidos por período", description = "Retorna pedidos em um período específico")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getOrdersByDateRange(startDate, endDate));
    }

    @GetMapping("/upcoming-deliveries")
    @Operation(summary = "Próximas entregas", description = "Retorna pedidos com entrega programada no período")
    public ResponseEntity<List<OrderDTO>> getUpcomingDeliveries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getUpcomingDeliveries(startDate, endDate));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Receita total", description = "Calcula receita total em um período")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getTotalRevenue(startDate, endDate));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Listar pedidos do cliente autenticado",
            description = "Retorna os pedidos vinculados ao e-mail do usuario logado"
    )

    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(email, pageable));
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Cria um novo pedido no sistema")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido e devolve estoque")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}