package com.artecomcarinho.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TurnstileService {

    @Value("${cloudflare.turnstile.secret}")
    private String turnstileSecret;

    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", turnstileSecret);
        params.add("response", token);

        try {
            Map response = restTemplate.postForObject(
                    TURNSTILE_VERIFY_URL,
                    params,
                    Map.class
            );

            if (response == null) return false;

            Boolean success = (Boolean) response.get("success");
            return Boolean.TRUE.equals(success);

        } catch (Exception e) {
            // Em produção, use um Logger aqui (ex: log.error("Erro Turnstile", e))
            e.printStackTrace();
            return false;
        }
    }
}