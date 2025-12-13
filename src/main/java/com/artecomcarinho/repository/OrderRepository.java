package com.artecomcarinho.repository;

import com.artecomcarinho.dto.CustomerKpiDTO;
import com.artecomcarinho.dto.OrderStatusStatsDTO;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Usado em getOrdersByDateRange
    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    // Usado em getUpcomingDeliveries
    @Query("select o from Order o where o.expectedDeliveryDate between :startDate and :endDate")
    List<Order> findUpcomingDeliveries(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Usado em getTotalRevenue
    @Query("select coalesce(sum(o.totalAmount), 0) " +
            "from Order o " +
            "where o.orderDate between :startDate and :endDate")
    BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT new com.artecomcarinho.dto.OrderStatusStatsDTO(o.status, COUNT(o)) " +
            "FROM Order o GROUP BY o.status")
    List<OrderStatusStatsDTO> countOrdersByStatus();

    @Query("""
        SELECT new com.artecomcarinho.dto.CustomerKpiDTO(
            o.customer.id,
            COUNT(o),
            COALESCE(CAST(SUM(o.totalAmount) AS double), 0.0), // MUDANÇA: CAST(SUM(o.totalAmount) AS double)
            MAX(o.orderDate)
        )
        FROM Order o
        GROUP BY o.customer.id
    """)
    List<CustomerKpiDTO> getCustomerKpis();
}
