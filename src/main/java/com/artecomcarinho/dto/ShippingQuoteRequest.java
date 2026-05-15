package com.artecomcarinho.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ShippingQuoteRequest {

    @JsonProperty("zipCode")
    @NotBlank(message = "CEP de destino e obrigatorio")
    @Pattern(regexp = "\\d{8}", message = "CEP de destino invalido")
    private String toZip;

    @Valid
    @Size(max = 50, message = "Quantidade maxima de itens excedida")
    private List<Item> items;

    public String getToZip() {
        return toZip;
    }

    public void setToZip(String toZip) {
        this.toZip = toZip;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        @JsonProperty("productId")
        private Long productId;

        @JsonProperty("quantity")
        @Min(value = 1, message = "Quantidade minima e 1")
        @Max(value = 100, message = "Quantidade maxima por item excedida")
        private Integer qty;

        private String sku;
        private Double weight;
        private Integer width;
        private Integer height;
        private Integer length;
        private Double price;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQty() {
            return qty;
        }

        public void setQty(Integer qty) {
            this.qty = qty;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }
}
