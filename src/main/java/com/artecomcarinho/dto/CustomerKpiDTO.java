package com.artecomcarinho.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerKpiDTO(
        Long customerId,
        Long ordersCount,
        BigDecimal totalRevenue,
        LocalDateTime lastOrderDate
) {}
