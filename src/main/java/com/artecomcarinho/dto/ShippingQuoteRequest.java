package com.artecomcarinho.dto;

import lombok.Data;
import java.util.List;

@Data
public class ShippingQuoteRequest {
    private String toZip;
    private List<Item> items;

    @Data
    public static class Item {
        private String sku;
        private Integer qty;
        private Double weight; // kg
        private Integer width;  // cm
        private Integer height; // cm
        private Integer length; // cm
        private Double price;   // ajuda na cotação/seguro
    }
}

