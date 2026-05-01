package com.artecomcarinho.service;

import com.artecomcarinho.dto.CardPaymentRequest;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Payment;
import com.artecomcarinho.model.enums.PaymentProvider;
import com.artecomcarinho.model.enums.PaymentStatus;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.PaymentRepository;
import com.artecomcarinho.security.AccessControlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MercadoPagoPixService {

    @Value("${mercadopago.accessToken}")
    private String accessToken;

    @Value("${mercadopago.baseUrl:https://api.mercadopago.com}")
    private String baseUrl;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final AccessControlService accessControlService;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Payment createPixPayment(Long orderId, Authentication authentication) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado: " + orderId));
        accessControlService.ensureOrderAccess(authentication, order);

        String url = baseUrl + "/v1/payments";

        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", order.getTotalAmount().doubleValue());
        body.put("description", "Pedido #" + order.getId());
        body.put("payment_method_id", "pix");

        Map<String, Object> payer = new HashMap<>();
        String email = order.getCustomer() != null && order.getCustomer().getEmail() != null
                ? order.getCustomer().getEmail()
                : "comprador@exemplo.com";
        payer.put("email", email);
        body.put("payer", payer);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("Erro MP (PIX): " + e.getResponseBodyAsString());
        }

        Map<String, Object> data = response.getBody();
        if (data == null) {
            throw new RuntimeException("Resposta vazia do Mercado Pago");
        }

        String externalId = String.valueOf(data.get("id"));
        String mpStatus = String.valueOf(data.get("status"));

        Map<String, Object> pointOfInteraction = (Map<String, Object>) data.get("point_of_interaction");
        Map<String, Object> transactionData = pointOfInteraction == null
                ? null
                : (Map<String, Object>) pointOfInteraction.get("transaction_data");

        String qrCode = transactionData == null ? null : String.valueOf(transactionData.get("qr_code"));
        String qrCodeBase64 = transactionData == null ? null : String.valueOf(transactionData.get("qr_code_base64"));

        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.MERCADO_PAGO)
                .status(mapStatus(mpStatus))
                .externalPaymentId(externalId)
                .pixQrCode(qrCode)
                .pixQrCodeBase64(qrCodeBase64)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (payment.getStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.IN_PRODUCTION);
            orderRepository.save(order);
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createCardPayment(CardPaymentRequest request, Authentication authentication) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado: " + request.getOrderId()));
        accessControlService.ensureOrderAccess(authentication, order);

        String url = baseUrl + "/v1/payments";

        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", order.getTotalAmount().doubleValue());
        body.put("token", request.getToken());
        body.put("description", "Pedido #" + order.getOrderNumber());
        body.put("installments", request.getInstallments());
        body.put("payment_method_id", request.getPaymentMethodId());

        if (request.getIssuerId() != null && !request.getIssuerId().trim().isEmpty() && !"null".equals(request.getIssuerId())) {
            body.put("issuer_id", request.getIssuerId());
        }

        Map<String, Object> payer = new HashMap<>();
        payer.put("email", request.getEmail());
        body.put("payer", payer);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("Erro MP: " + e.getResponseBodyAsString());
        }

        Map<String, Object> data = response.getBody();
        if (data == null) {
            throw new RuntimeException("Erro ao processar cartao");
        }

        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.MERCADO_PAGO)
                .status(mapStatus(String.valueOf(data.get("status"))))
                .externalPaymentId(String.valueOf(data.get("id")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (payment.getStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.IN_PRODUCTION);
            orderRepository.save(order);
        }

        return paymentRepository.save(payment);
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.PAID;
            case "cancelled", "rejected" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.PENDING;
        };
    }
}
