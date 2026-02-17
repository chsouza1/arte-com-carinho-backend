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
public class HcaptchaService {

    @Value("${hcaptcha.secret}")
    private String hCaptchaSecret;

    private static final String HCAPTCHA_VERIFY_URL = "https://api.hcaptcha.com/siteverify";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", hCaptchaSecret);
        params.add("response", token);

        try {
            Map response = restTemplate.postForObject(
                    HCAPTCHA_VERIFY_URL,
                    params,
                    Map.class
            );

            if (response == null) return false;

            Boolean success = (Boolean) response.get("success");
            return Boolean.TRUE.equals(success);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}