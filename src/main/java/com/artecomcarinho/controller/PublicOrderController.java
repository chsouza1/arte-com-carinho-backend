package com.artecomcarinho.controller;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.dto.PublicOrderRequest;
import com.artecomcarinho.security.RateLimitService;
import com.artecomcarinho.service.PublicOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/public/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Public Orders", description = "Criacao de pedidos a partir do site")
public class PublicOrderController {

    private final PublicOrderService publicOrderService;
    private final RateLimitService rateLimitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar pedido publico",
            description = "Cria um novo pedido a partir dos dados informados pelo cliente no site"
    )
    public OrderDTO createPublicOrder(
            HttpServletRequest httpRequest,
            @Valid @RequestBody PublicOrderRequest request) {
        String email = request.getCustomer() == null ? null : request.getCustomer().getEmail();
        rateLimitService.check("public:order", rateLimitService.key(httpRequest, email), 10, Duration.ofHours(1));
        return publicOrderService.createPublicOrder(request);
    }
}
