package com.artecomcarinho.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingQuoteOption {

    private String provider;

    @JsonProperty("name")
    private String service;

    private Double price;

    @JsonProperty("delivery_time")
    private Integer days;

    @JsonProperty("id")
    private String rawId;
}