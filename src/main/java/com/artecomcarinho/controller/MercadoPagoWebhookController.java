package com.artecomcarinho.controller;

import com.artecomcarinho.model.Payment;
import com.artecomcarinho.model.enums.PaymentStatus;
import com.artecomcarinho.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    @Value("${mercadopago.accessToken}")
    private String accessToken;

    @Value("${mercadopago.baseUrl:https://api.mercadopago.com}")
    private String baseUrl;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<Void> webhook(@RequestBody Map<String, Object> payload) {
        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) return ResponseEntity.ok().build();

        Object idObj = data.get("id");
        if (idObj == null) return ResponseEntity.ok().build();

        String externalPaymentId = String.valueOf(idObj);

        // consulta status no MP
        String url = baseUrl + "/v1/payments/" + externalPaymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> pay = res.getBody();
        if (pay == null) return ResponseEntity.ok().build();

        String status = String.valueOf(pay.get("status"));


        Payment p = paymentRepository.findByExternalPaymentId(externalPaymentId).orElse(null);
        if (p == null) return ResponseEntity.ok().build();

        PaymentStatus newStatus = switch (status.toLowerCase()) {
            case "approved" -> PaymentStatus.PAID;
            case "cancelled", "rejected" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.PENDING;
        };

        p.setStatus(newStatus);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        return ResponseEntity.ok().build();
    }
}
