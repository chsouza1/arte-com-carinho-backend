package com.artecomcarinho.dto;

import com.artecomcarinho.model.Order.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    private String orderNumber;

    @NotNull(message = "Cliente é obrigatório")
    private Long customerId;

    private String customerName;

    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    @NotNull(message = "Status é obrigatório")
    private OrderStatus status;

    @NotNull(message = "Total é obrigatório")
    @DecimalMin(value = "0.01")
    private BigDecimal totalAmount;

    private BigDecimal discount;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private LocalDate deliveredDate;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private String notes;

    private String customizationDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDTO {

        private Long id;

        @NotNull(message = "Produto é obrigatório")
        private Long productId;

        private String productName;

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1)
        private Integer quantity;

        @NotNull(message = "Preço unitário é obrigatório")
        @DecimalMin(value = "0.01")
        private BigDecimal unitPrice;

        private BigDecimal subtotal;

        private String selectedSize;

        private String selectedColor;

        private String customizationNotes;
    }
}