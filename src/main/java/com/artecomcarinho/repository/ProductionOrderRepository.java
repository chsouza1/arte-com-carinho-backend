package com.artecomcarinho.repository;

import com.artecomcarinho.model.ProductionOrder;
import com.artecomcarinho.model.enums.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findByStageOrderByUpdatedAtDesc(ProductionStage stage);

}
