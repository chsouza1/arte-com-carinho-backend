package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryStatsDTO {

    /**
     * Soma do valor total dos pedidos entregues no período.
     */
    private BigDecimal totalRevenue;

    /**
     * Quantidade de pedidos entregues no período.
     */
    private long totalOrders;

    /**
     * Ticket médio = totalRevenue / totalOrders (0 se não houver pedidos).
     */
    private BigDecimal avgTicket;
}
