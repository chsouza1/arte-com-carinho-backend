package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ShippingQuoteOption {
    private String provider;   // MELHOR_ENVIO
    private String service;    // PAC/SEDEX/Jadlog etc
    private Double price;
    private Integer days;
    private String rawId;      // id do serviço no ME (se quiser)
}

