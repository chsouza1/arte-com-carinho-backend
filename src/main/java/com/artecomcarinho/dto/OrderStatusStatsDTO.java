package com.artecomcarinho.dto;

import com.artecomcarinho.model.Order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusStatsDTO {
    private OrderStatus status;
    private Long count;
}