package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class PixPaymentResponse {
    private Long orderId;
    private String paymentId;      // external id
    private String qrCode;         // copia e cola
    private String qrCodeBase64;   // imagem base64
    private String status;         // pending/approved...
}
