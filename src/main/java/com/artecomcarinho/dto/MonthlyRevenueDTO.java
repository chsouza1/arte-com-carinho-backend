package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDTO {

    /**
     * Rótulo do mês: "Jan", "Fev", etc.
     */
    private String month;

    /**
     * Faturamento (pedidos entregues) no mês.
     */
    private BigDecimal revenue;

    /**
     * Quantidade de pedidos entregues no mês.
     */
    private long orders;
}
