package com.artecomcarinho.service;

import com.artecomcarinho.dto.ShippingQuoteOption;
import com.artecomcarinho.dto.ShippingQuoteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MelhorEnvioClient {

    @Value("${melhorenvio.baseUrl}")
    private String baseUrl;

    @Value("${melhorenvio.token}")
    private String token;

    @Value("${melhorenvio.fromZip}")
    private String fromZip;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<ShippingQuoteOption> quote(ShippingQuoteRequest req) {
        // Endpoint/contrato conforme “Cotação de fretes” do Melhor Envio :contentReference[oaicite:3]{index=3}
        String url = baseUrl + "/me/shipment/calculate";

        Map<String, Object> body = new HashMap<>();
        body.put("from", Map.of("postal_code", fromZip));
        body.put("to", Map.of("postal_code", req.getToZip()));

        // produtos => ME pode empacotar automaticamente :contentReference[oaicite:4]{index=4}
        List<Map<String, Object>> products = new ArrayList<>();
        for (var it : req.getItems()) {
            products.add(Map.of(
                    "id", it.getSku(),
                    "width", it.getWidth(),
                    "height", it.getHeight(),
                    "length", it.getLength(),
                    "weight", it.getWeight(),
                    "insurance_value", it.getPrice() == null ? 0 : it.getPrice(),
                    "quantity", it.getQty() == null ? 1 : it.getQty()
            ));
        }
        body.put("products", products);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        var http = new HttpEntity<>(body, headers);
        ResponseEntity<List> res = restTemplate.exchange(url, HttpMethod.POST, http, List.class);

        List<Map<String, Object>> list = (List<Map<String, Object>>) res.getBody();
        if (list == null) return List.of();

        List<ShippingQuoteOption> out = new ArrayList<>();
        for (var opt : list) {
            // os campos exatos podem variar; ajuste conforme resposta real da API
            String name = String.valueOf(opt.getOrDefault("name", opt.get("service")));
            Double price = toDouble(opt.get("price"));
            Integer days = toInt(opt.get("delivery_time"));
            String id = String.valueOf(opt.getOrDefault("id", ""));
            out.add(new ShippingQuoteOption("MELHOR_ENVIO", name, price, days, id));
        }
        return out;
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        try { return Double.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private Integer toInt(Object v) {
        if (v == null) return null;
        try { return Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
