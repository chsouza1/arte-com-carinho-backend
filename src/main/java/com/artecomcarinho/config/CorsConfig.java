package com.artecomcarinho.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        validateOrigins(origins);

        config.setAllowedOrigins(origins);
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private void validateOrigins(List<String> origins) {
        if (origins.isEmpty()) {
            throw new IllegalStateException("cors.allowed-origins deve ter pelo menos uma origem");
        }

        for (String origin : origins) {
            if ("*".equals(origin)) {
                throw new IllegalStateException("CORS com credenciais nao pode usar origem wildcard '*'");
            }

            if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
                throw new IllegalStateException("Origem CORS invalida: " + origin);
            }
        }
    }
}
