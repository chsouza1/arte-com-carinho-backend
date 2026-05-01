package com.artecomcarinho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicOrderRequest {

    @NotNull(message = "Dados do cliente sao obrigatorios")
    @Valid
    private CustomerInfo customer;

    @NotEmpty(message = "E necessario informar pelo menos um item")
    @Valid
    private List<OrderItemRequest> items;

    private String notes;

    private String paymentMethod;

    private BigDecimal shippingCost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        @NotBlank(message = "Nome do cliente e obrigatorio")
        private String name;

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        private String email;

        @NotBlank(message = "Telefone/WhatsApp e obrigatorio")
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Produto e obrigatorio")
        private Long productId;

        @NotNull(message = "Quantidade e obrigatoria")
        @Min(value = 1, message = "Quantidade minima e 1")
        private Integer quantity;

        private String selectedSize;
        private String selectedColor;
        private String customizationNotes;
    }
}
