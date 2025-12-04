package com.artecomcarinho.service;

import com.artecomcarinho.dto.MonthlyRevenueDTO;
import com.artecomcarinho.dto.OrderSummaryStatsDTO;
import com.artecomcarinho.dto.TopProductStatsDTO;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.OrderItem;
import com.artecomcarinho.model.Order.OrderStatus;
import com.artecomcarinho.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatsService {

    private final OrderRepository orderRepository;

    /**
     * Resumo de faturamento/ticket/pedidos entre duas datas.
     * Considera apenas pedidos com status DELIVERED.
     */
    public OrderSummaryStatsDTO getSummary(LocalDate start, LocalDate end) {
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();

        BigDecimal avgTicket = BigDecimal.ZERO;
        if (totalOrders > 0) {
            avgTicket = totalRevenue
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        return new OrderSummaryStatsDTO(totalRevenue, totalOrders, avgTicket);
    }

    /**
     * Faturamento mensal e quantidade de pedidos por mês de um ano.
     * Considera apenas pedidos DELIVERED.
     */
    public List<MonthlyRevenueDTO> getRevenueByMonth(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<Order> orders = orderRepository.findByOrderDateBetween(start, end)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        // acumulador por mês (1 a 12)
        Map<Integer, MonthAccumulator> map = new HashMap<>();

        for (Order order : orders) {
            if (order.getOrderDate() == null) continue;

            int month = order.getOrderDate().getMonthValue();
            MonthAccumulator acc = map.computeIfAbsent(month, m -> new MonthAccumulator());
            acc.orders++;
            BigDecimal amount = Optional.ofNullable(order.getTotalAmount()).orElse(BigDecimal.ZERO);
            acc.revenue = acc.revenue.add(amount);
        }

        // converte para DTO, em ordem crescente de mês
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyRevenueDTO(
                        monthLabel(e.getKey()),
                        e.getValue().revenue,
                        e.getValue().orders
                ))
                .collect(Collectors.toList());
    }

    /**
     * Top N produtos mais vendidos por quantidade no período.
     * Baseado em itens de pedidos DELIVERED.
     */
    public List<TopProductStatsDTO> getTopProducts(LocalDate start, LocalDate end, int limit) {
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.toList());

        Map<String, ProductAccumulator> map = new HashMap<>();

        for (Order order : orders) {
            if (order.getItems() == null) continue;

            for (OrderItem item : order.getItems()) {
                if (item.getProduct() == null) continue;

                String name = item.getProduct().getName();
                ProductAccumulator acc = map.computeIfAbsent(name, n -> new ProductAccumulator());

                int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
                acc.totalSold += qty;

                BigDecimal subtotal = Optional.ofNullable(item.getSubtotal()).orElse(BigDecimal.ZERO);
                acc.totalRevenue = acc.totalRevenue.add(subtotal);
            }
        }

        return map.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().totalSold, e1.getValue().totalSold))
                .limit(limit)
                .map(e -> new TopProductStatsDTO(
                        e.getKey(),
                        e.getValue().totalSold,
                        e.getValue().totalRevenue
                ))
                .collect(Collectors.toList());
    }

    private String monthLabel(int month) {
        return switch (month) {
            case 1 -> "Jan";
            case 2 -> "Fev";
            case 3 -> "Mar";
            case 4 -> "Abr";
            case 5 -> "Mai";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Ago";
            case 9 -> "Set";
            case 10 -> "Out";
            case 11 -> "Nov";
            case 12 -> "Dez";
            default -> String.valueOf(month);
        };
    }

    private static class MonthAccumulator {
        private BigDecimal revenue = BigDecimal.ZERO;
        private long orders = 0;
    }

    private static class ProductAccumulator {
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private long totalSold = 0;
    }
}
