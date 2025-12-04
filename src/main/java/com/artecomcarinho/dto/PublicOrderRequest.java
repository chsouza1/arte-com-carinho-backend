package com.artecomcarinho.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicOrderRequest {

    @NotNull(message = "Dados do cliente são obrigatórios")
    private CustomerInfo customer;

    @NotEmpty(message = "É necessário informar pelo menos um item")
    private List<OrderItemRequest> items;

    private String notes;

    /**
     * Ex.: PIX, CARD, CASH...
     * Pode ser nulo se você quiser definir isso depois no fluxo interno.
     */
    private String paymentMethod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        @NotBlank(message = "Nome do cliente é obrigatório")
        private String name;

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Telefone/WhatsApp é obrigatório")
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Produto é obrigatório")
        private Long productId;

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade mínima é 1")
        private Integer quantity;

        private String selectedSize;
        private String selectedColor;
        private String customizationNotes;
    }
}
