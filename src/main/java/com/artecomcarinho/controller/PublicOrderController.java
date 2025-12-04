package com.artecomcarinho.controller;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.dto.PublicOrderRequest;
import com.artecomcarinho.service.PublicOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Public Orders", description = "Criação de pedidos a partir do site (cliente final)")
public class PublicOrderController {

    private final PublicOrderService publicOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar pedido público",
            description = "Cria um novo pedido a partir dos dados informados pelo cliente no site"
    )
    public OrderDTO createPublicOrder(@Valid @RequestBody PublicOrderRequest request) {
        return publicOrderService.createPublicOrder(request);
    }
}
