package com.artecomcarinho.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Captcha e obrigatorio")
    private String captchaToken;

    @Size(max = 1000, message = "Observacoes devem ter no maximo 1000 caracteres")
    private String notes;

    @Size(max = 30, message = "Forma de pagamento invalida")
    private String paymentMethod;

    @DecimalMin(value = "0.00", message = "Frete nao pode ser negativo")
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
        @Size(min = 10, max = 15, message = "Telefone invalido")
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

        @Size(max = 50, message = "Tamanho invalido")
        private String selectedSize;

        @Size(max = 50, message = "Cor invalida")
        private String selectedColor;

        @Size(max = 500, message = "Observacoes de personalizacao muito longas")
        private String customizationNotes;
    }
}
