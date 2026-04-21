package com.artecomcarinho.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardPaymentRequest {
    private Long orderId;
    private String token;
    private String paymentMethodId;
    private Integer installments;
    private String issuerId;
    private String email;
}