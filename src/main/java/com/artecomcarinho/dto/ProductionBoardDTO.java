package com.artecomcarinho.dto;

import com.artecomcarinho.model.enums.ProductionStage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ProductionBoardDTO {
    private Map<ProductionStage, List<ProductionCardDTO>> columns;
}
