package com.artecomcarinho.controller;

import com.artecomcarinho.dto.CardPaymentRequest;
import com.artecomcarinho.model.Payment;
import com.artecomcarinho.service.MercadoPagoPixService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final MercadoPagoPixService pixService;

    @PostMapping("/pix")
    public PixResponse createPix(@RequestParam Long orderId, Authentication authentication) {
        Payment payment = pixService.createPixPayment(orderId, authentication);
        return new PixResponse(
                payment.getOrder().getId(),
                payment.getId(),
                payment.getExternalPaymentId(),
                payment.getStatus().name(),
                payment.getPixQrCode(),
                payment.getPixQrCodeBase64()
        );
    }

    @PostMapping("/card")
    public ResponseEntity<Payment> createCardPayment(
            @Valid @RequestBody CardPaymentRequest request,
            Authentication authentication) {
        Payment payment = pixService.createCardPayment(request, authentication);
        return ResponseEntity.ok(payment);
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
