package com.artecomcarinho.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPaymentRequest {

    @NotNull(message = "Pedido e obrigatorio")
    private Long orderId;

    @NotBlank(message = "Token do cartao e obrigatorio")
    private String token;

    @NotBlank(message = "Bandeira do pagamento e obrigatoria")
    private String paymentMethodId;

    @NotNull(message = "Numero de parcelas e obrigatorio")
    @Min(value = 1, message = "Numero de parcelas invalido")
    @Max(value = 24, message = "Numero de parcelas invalido")
    private Integer installments;

    private String issuerId;

    @NotBlank(message = "E-mail do pagador e obrigatorio")
    @Email(message = "E-mail do pagador invalido")
    private String email;
}
