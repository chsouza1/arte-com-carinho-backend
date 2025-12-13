package com.artecomcarinho.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerKpiDTO(
        Long customerId,
        Long ordersCount,
        BigDecimal totalRevenue,
        LocalDate lastOrderDate
) {}
