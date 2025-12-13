package com.artecomcarinho.model;

import com.artecomcarinho.model.enums.PaymentProvider;
import com.artecomcarinho.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos pagamentos podem existir para um pedido (caso re-tente)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // id do pagamento no Mercado Pago
    @Column(length = 80, unique = true)
    private String externalPaymentId;

    @Column(columnDefinition = "TEXT")
    private String pixQrCode;

    @Column(columnDefinition = "TEXT")
    private String pixQrCodeBase64;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
