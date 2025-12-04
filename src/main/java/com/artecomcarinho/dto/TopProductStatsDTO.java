package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductStatsDTO {

    /**
     * Nome do produto.
     */
    private String productName;

    /**
     * Quantidade total vendida no período.
     */
    private long totalSold;

    /**
     * Faturamento gerado por este produto no período.
     */
    private BigDecimal totalRevenue;
}
