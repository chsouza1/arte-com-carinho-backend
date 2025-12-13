package com.artecomcarinho.controller;

import com.artecomcarinho.model.Payment;
import com.artecomcarinho.service.MercadoPagoPixService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final MercadoPagoPixService pixService;

    @PostMapping("/pix")
    public PixResponse createPix(@RequestParam Long orderId) {
        Payment p = pixService.createPixPayment(orderId);
        return new PixResponse(
                p.getOrder().getId(),
                p.getId(),
                p.getExternalPaymentId(),
                p.getStatus().name(),
                p.getPixQrCode(),
                p.getPixQrCodeBase64()
        );
    }

    @Data
    @AllArgsConstructor
    public static class PixResponse {
        private Long orderId;
        private Long paymentId;
        private String externalPaymentId;
        private String status;
        private String qrCode;
        private String qrCodeBase64;
    }
}
