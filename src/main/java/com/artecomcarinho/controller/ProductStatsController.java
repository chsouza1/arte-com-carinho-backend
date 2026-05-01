package com.artecomcarinho.controller;

import com.artecomcarinho.dto.TopProductStatsDTO;
import com.artecomcarinho.service.OrderStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Product Stats", description = "Relatórios de desempenho de produtos")
public class ProductStatsController {

    private final OrderStatsService orderStatsService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top")
    @Operation(
            summary = "Top produtos",
            description = "Retorna os produtos mais vendidos em um intervalo de datas"
    )
    public List<TopProductStatsDTO> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return orderStatsService.getTopProducts(start, end, limit);
    }
}
