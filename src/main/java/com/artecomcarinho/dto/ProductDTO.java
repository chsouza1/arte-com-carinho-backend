package com.artecomcarinho.dto;

import com.artecomcarinho.model.Product.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    private String description;

    @NotNull(message = "Categoria é obrigatória")
    private ProductCategory category;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Min(value = 0)
    private Integer stock;

    private String sku;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private List<String> sizes = new ArrayList<>();

    @Builder.Default
    private List<String> colors = new ArrayList<>();

    private Boolean active;

    private Boolean featured;

    private Boolean customizable;
}