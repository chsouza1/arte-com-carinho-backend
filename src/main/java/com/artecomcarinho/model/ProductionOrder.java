package com.artecomcarinho.model;

import com.artecomcarinho.model.enums.ProductionStage;
import com.artecomcarinho.model.enums.ProductionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder {

    @Id
    private Long orderId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime updatedAt;
}
