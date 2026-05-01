package com.artecomcarinho.controller;

import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Payment;
import com.artecomcarinho.model.enums.PaymentStatus;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@ConditionalOnProperty(name = "mercadopago.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/webhooks/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {

    @Value("${mercadopago.accessToken}")
    private String accessToken;

    @Value("${mercadopago.baseUrl:https://api.mercadopago.com}")
    private String baseUrl;

    @Value("${mercadopago.webhookSecret:}")
    private String webhookSecret;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            HttpServletRequest request,
            @RequestBody Map<String, Object> payload) {
        if (!isValidWebhookSignature(xSignature, xRequestId, request)) {
            log.warn("Webhook Mercado Pago rejeitado por assinatura invalida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            return ResponseEntity.ok().build();
        }

        Object idObj = data.get("id");
        if (idObj == null) {
            return ResponseEntity.ok().build();
        }

        String externalPaymentId = String.valueOf(idObj);
        String url = baseUrl + "/v1/payments/" + externalPaymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> paymentData = response.getBody();
        if (paymentData == null) {
            return ResponseEntity.ok().build();
        }

        String status = String.valueOf(paymentData.get("status"));
        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId).orElse(null);
        if (payment == null) {
            return ResponseEntity.ok().build();
        }

        PaymentStatus newStatus = switch (status.toLowerCase()) {
            case "approved" -> PaymentStatus.PAID;
            case "cancelled", "rejected" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.PENDING;
        };

        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (newStatus == PaymentStatus.PAID) {
            Order order = payment.getOrder();
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.IN_PRODUCTION);
            orderRepository.save(order);
        }

        return ResponseEntity.ok().build();
    }

    private boolean isValidWebhookSignature(String xSignature, String xRequestId, HttpServletRequest request) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return true;
        }

        if (xSignature == null || xSignature.isBlank()) {
            return false;
        }

        Map<String, String> signatureParts = parseSignatureHeader(xSignature);
        String ts = signatureParts.get("ts");
        String receivedHash = signatureParts.get("v1");

        if (ts == null || ts.isBlank() || receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        String manifest = buildManifest(request.getParameter("data.id"), xRequestId, ts);
        String expectedHash = hmacSha256Hex(webhookSecret, manifest);
        String normalizedReceivedHash = receivedHash.toLowerCase(Locale.ROOT);

        return MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                normalizedReceivedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private Map<String, String> parseSignatureHeader(String xSignature) {
        Map<String, String> parts = new HashMap<>();
        for (String item : xSignature.split(",")) {
            String[] keyValue = item.split("=", 2);
            if (keyValue.length == 2) {
                parts.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return parts;
    }

    private String buildManifest(String dataId, String requestId, String timestamp) {
        StringBuilder manifest = new StringBuilder();

        if (dataId != null && !dataId.isBlank()) {
            manifest.append("id:").append(dataId.toLowerCase(Locale.ROOT)).append(';');
        }
        if (requestId != null && !requestId.isBlank()) {
            manifest.append("request-id:").append(requestId).append(';');
        }
        manifest.append("ts:").append(timestamp).append(';');

        return manifest.toString();
    }

    private String hmacSha256Hex(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Nao foi possivel validar a assinatura do webhook", e);
        }
    }
}
