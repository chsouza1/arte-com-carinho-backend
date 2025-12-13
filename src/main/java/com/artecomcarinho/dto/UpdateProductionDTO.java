package com.artecomcarinho.dto;

import com.artecomcarinho.model.enums.ProductionStage;
import com.artecomcarinho.model.enums.ProductionStatus;
import lombok.Data;

@Data
public class UpdateProductionDTO {
    private ProductionStage stage;
    private ProductionStatus status;
    private String notes;
}
