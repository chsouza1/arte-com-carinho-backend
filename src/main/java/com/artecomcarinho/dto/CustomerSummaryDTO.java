package com.artecomcarinho.dto;

import java.time.LocalDateTime;

public class CustomerSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public CustomerSummaryDTO(Long id, String name, String email, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // getters e setters
}
