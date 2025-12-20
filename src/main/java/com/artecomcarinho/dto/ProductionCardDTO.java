package com.artecomcarinho.dto;

import com.artecomcarinho.model.enums.ProductionStage;
import com.artecomcarinho.model.enums.ProductionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductionCardDTO {
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private ProductionStage stage;
    private ProductionStatus status;
    private String notes;
    private LocalDateTime updatedAt;
    private LocalDate expectedDeliveryDate;
    private List<ProductionCardItemDTO> items;
}
