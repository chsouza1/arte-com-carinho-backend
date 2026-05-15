package com.artecomcarinho.controller;

import com.artecomcarinho.dto.CardPaymentRequest;
import com.artecomcarinho.model.Payment;
import com.artecomcarinho.security.RateLimitService;
import com.artecomcarinho.service.MercadoPagoPixService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.time.Duration;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final MercadoPagoPixService pixService;
    private final RateLimitService rateLimitService;

    @PostMapping("/pix")
    public PixResponse createPix(
            HttpServletRequest httpRequest,
            @RequestParam Long orderId,
            Authentication authentication) {
        rateLimitService.check("payments:pix", paymentKey(httpRequest, authentication, orderId), 10, Duration.ofMinutes(15));
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
            HttpServletRequest httpRequest,
            @Valid @RequestBody CardPaymentRequest request,
            Authentication authentication) {
        rateLimitService.check("payments:card", paymentKey(httpRequest, authentication, request.getOrderId()), 10, Duration.ofMinutes(15));
        Payment payment = pixService.createCardPayment(request, authentication);
        return ResponseEntity.ok(payment);
    }

    private String paymentKey(HttpServletRequest request, Authentication authentication, Long orderId) {
        String user = authentication == null ? null : authentication.getName();
        String discriminator = (user == null ? "" : user) + ":order:" + orderId;
        return rateLimitService.key(request, discriminator);
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
