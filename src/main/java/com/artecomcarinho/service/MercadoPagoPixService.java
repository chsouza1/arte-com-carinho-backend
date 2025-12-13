package com.artecomcarinho.service;

import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Payment;
import com.artecomcarinho.model.enums.PaymentProvider;
import com.artecomcarinho.model.enums.PaymentStatus;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MercadoPagoPixService {

    @Value("${mercadopago.accessToken}")
    private String accessToken;

    @Value("${mercadopago.baseUrl:https://api.mercadopago.com}")
    private String baseUrl;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Payment createPixPayment(Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderId));

        String url = baseUrl + "/v1/payments";

        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", o.getTotalAmount().doubleValue());
        body.put("description", "Pedido #" + o.getId());
        body.put("payment_method_id", "pix");

        // payer (ajuste se tiver email no customer; se não tiver, use um placeholder)
        Map<String, Object> payer = new HashMap<>();
        String email = "comprador@exemplo.com";
        try {
            if (o.getCustomer() != null && o.getCustomer().getEmail() != null) {
                email = o.getCustomer().getEmail();
            }
        } catch (Exception ignored) {}
        payer.put("email", email);
        body.put("payer", payer);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> res = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map<String, Object> data = res.getBody();
        if (data == null) throw new RuntimeException("Resposta vazia do Mercado Pago");

        String externalId = String.valueOf(data.get("id"));
        String mpStatus = String.valueOf(data.get("status"));

        Map<String, Object> poi = (Map<String, Object>) data.get("point_of_interaction");
        Map<String, Object> tx = poi == null ? null : (Map<String, Object>) poi.get("transaction_data");

        String qrCode = tx == null ? null : String.valueOf(tx.get("qr_code"));
        String qrBase64 = tx == null ? null : String.valueOf(tx.get("qr_code_base64"));

        Payment payment = Payment.builder()
                .order(o)
                .provider(PaymentProvider.MERCADO_PAGO)
                .status(mapStatus(mpStatus))
                .externalPaymentId(externalId)
                .pixQrCode(qrCode)
                .pixQrCodeBase64(qrBase64)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.PAID;
            case "cancelled", "rejected" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.PENDING;
        };
    }
}
