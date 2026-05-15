package com.artecomcarinho.service;

import com.artecomcarinho.dto.ShippingQuoteOption;
import com.artecomcarinho.dto.ShippingQuoteRequest;
import com.artecomcarinho.exception.InvalidOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ConditionalOnProperty(name = "melhorenvio.enabled", havingValue = "true")
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
        String url = baseUrl + "/me/shipment/calculate";

        Map<String, Object> body = new HashMap<>();

        Map<String, Object> fromMap = new HashMap<>();
        fromMap.put("postal_code", fromZip);
        body.put("from", fromMap);

        Map<String, Object> toMap = new HashMap<>();
        toMap.put("postal_code", req.getToZip());
        body.put("to", toMap);

        List<Map<String, Object>> products = new ArrayList<>();
        if (req.getItems() != null) {
            for (var it : req.getItems()) {
                Map<String, Object> prod = new HashMap<>();
                prod.put("id", it.getSku() != null ? it.getSku() : "1");
                prod.put("width", it.getWidth() != null ? it.getWidth() : 20);
                prod.put("height", it.getHeight() != null ? it.getHeight() : 15);
                prod.put("length", it.getLength() != null ? it.getLength() : 20);
                prod.put("weight", it.getWeight() != null ? it.getWeight() : 0.3);
                prod.put("insurance_value", it.getPrice() == null ? 0 : it.getPrice());
                prod.put("quantity", it.getQty() == null ? 1 : it.getQty());
                products.add(prod);
            }
        }
        body.put("products", products);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> http = new HttpEntity<>(body, headers);

        ResponseEntity<List> res;
        try {
            res = restTemplate.exchange(url, HttpMethod.POST, http, List.class);
        } catch (HttpClientErrorException e) {
            log.warn("Falha ao consultar frete no Melhor Envio: {}", e.getResponseBodyAsString());
            throw new InvalidOperationException("Nao foi possivel consultar o frete");
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar frete", e);
            throw new InvalidOperationException("Nao foi possivel consultar o frete");
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) res.getBody();
        if (list == null) {
            return List.of();
        }

        List<ShippingQuoteOption> out = new ArrayList<>();
        for (var opt : list) {
            if (opt.containsKey("error") && opt.get("error") != null) {
                continue;
            }

            String name = String.valueOf(opt.getOrDefault("name", opt.get("service")));
            Double price = toDouble(opt.getOrDefault("custom_price", opt.get("price")));
            Integer days = toInt(opt.getOrDefault("custom_delivery_time", opt.get("delivery_time")));
            String id = String.valueOf(opt.getOrDefault("id", ""));

            out.add(new ShippingQuoteOption("MELHOR_ENVIO", name, price, days, id));
        }
        return out;
    }

    private Double toDouble(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Double.valueOf(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInt(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}
