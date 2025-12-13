package com.artecomcarinho.controller;

import com.artecomcarinho.dto.MonthlyRevenueDTO;
import com.artecomcarinho.dto.OrderStatusStatsDTO;
import com.artecomcarinho.dto.OrderSummaryStatsDTO;
import com.artecomcarinho.service.OrderStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Order Stats", description = "Relatórios e estatísticas de pedidos")
public class OrderStatsController {

    private final OrderStatsService orderStatsService;

    @GetMapping("/summary")
    @Operation(
            summary = "Resumo de faturamento",
            description = "Retorna faturamento, número de pedidos e ticket médio em um intervalo de datas"
    )
    public OrderSummaryStatsDTO getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return orderStatsService.getSummary(start, end);
    }

    @GetMapping("/by-month")
    @Operation(
            summary = "Faturamento por mês",
            description = "Retorna faturamento e quantidade de pedidos por mês no ano informado"
    )
    public List<MonthlyRevenueDTO> getRevenueByMonth(@RequestParam int year) {
        return orderStatsService.getRevenueByMonth(year);
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "Pedidos por status", description = "Quantidade de pedidos agrupados por status")
    public List<OrderStatusStatsDTO> getStatusDistribution() {
        return orderStatsService.getOrdersByStatusStats();
    }
}
