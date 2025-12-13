package com.artecomcarinho.service;

import com.artecomcarinho.dto.*;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.model.ProductionOrder;
import com.artecomcarinho.model.enums.ProductionStage;
import com.artecomcarinho.model.enums.ProductionStatus;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.ProductionOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionOrderRepository productionOrderRepository;
    private final OrderRepository orderRepository;

    private void ensureProductionForAllOrders() {
        List<Order> orders = orderRepository.findAll();

        List<ProductionOrder> toCreate = new ArrayList<>();

        for (Order order : orders) {
            Long id = order.getId();
            if (id == null) continue;

            if (!productionOrderRepository.existsById(id)) {
                ProductionOrder po = ProductionOrder.builder()
                        .order(order)
                        .stage(ProductionStage.BORDADO)
                        .status(ProductionStatus.PENDING)
                        .notes(null)
                        .updatedAt(LocalDateTime.now())
                        .build();
                toCreate.add(po);
            }
        }

        if (!toCreate.isEmpty()) {
            productionOrderRepository.saveAll(toCreate);
        }
    }

    public ProductionBoardDTO getBoard() {
        ensureProductionForAllOrders();

        Map<ProductionStage, List<ProductionCardDTO>> map = new EnumMap<>(ProductionStage.class);

        for (ProductionStage stage : ProductionStage.values()) {
            List<ProductionOrder> list = productionOrderRepository.findByStageOrderByUpdatedAtDesc(stage);
            List<ProductionCardDTO> cards = list.stream().map(this::toCard).toList();
            map.put(stage, cards);
        }

        return new ProductionBoardDTO(map);
    }


    public ProductionCardDTO ensureAndGet(Long orderId) {
        ProductionOrder po = productionOrderRepository.findById(orderId)
                .orElseGet(() -> createDefault(orderId));
        return toCard(po);
    }

    public ProductionCardDTO update(Long orderId, UpdateProductionDTO dto) {
        ProductionOrder po = productionOrderRepository.findById(orderId)
                .orElseGet(() -> createDefault(orderId));

        if (dto.getStage() != null) po.setStage(dto.getStage());
        if (dto.getStatus() != null) po.setStatus(dto.getStatus());
        if (dto.getNotes() != null) po.setNotes(dto.getNotes());

        po.setUpdatedAt(LocalDateTime.now());
        productionOrderRepository.save(po);

        return toCard(po);
    }

    public ProductionCardDTO moveNext(Long orderId) {
        ProductionOrder po = productionOrderRepository.findById(orderId)
                .orElseGet(() -> createDefault(orderId));

        po.setStage(nextStage(po.getStage()));
        po.setUpdatedAt(LocalDateTime.now());
        productionOrderRepository.save(po);

        return toCard(po);
    }

    public ProductionCardDTO movePrev(Long orderId) {
        ProductionOrder po = productionOrderRepository.findById(orderId)
                .orElseGet(() -> createDefault(orderId));

        po.setStage(prevStage(po.getStage()));
        po.setUpdatedAt(LocalDateTime.now());
        productionOrderRepository.save(po);

        return toCard(po);
    }

    private ProductionOrder createDefault(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + orderId));

        ProductionOrder po = ProductionOrder.builder()
                .order(order)
                .stage(ProductionStage.BORDADO)
                .status(ProductionStatus.PENDING)
                .notes(null)
                .updatedAt(LocalDateTime.now())
                .build();

        return productionOrderRepository.save(po);
    }

    private ProductionStage nextStage(ProductionStage current) {
        return switch (current) {
            case BORDADO -> ProductionStage.COSTURA;
            case COSTURA -> ProductionStage.ACABAMENTO;
            case ACABAMENTO -> ProductionStage.EMBALAGEM;
            case EMBALAGEM -> ProductionStage.CONCLUIDO;
            case CONCLUIDO -> ProductionStage.CONCLUIDO;
        };
    }

    private ProductionStage prevStage(ProductionStage current) {
        return switch (current) {
            case BORDADO -> ProductionStage.BORDADO;
            case COSTURA -> ProductionStage.BORDADO;
            case ACABAMENTO -> ProductionStage.COSTURA;
            case EMBALAGEM -> ProductionStage.ACABAMENTO;
            case CONCLUIDO -> ProductionStage.EMBALAGEM;
        };
    }

    private ProductionCardDTO toCard(ProductionOrder po) {
        Order o = po.getOrder();

        String customerName =
                (o.getCustomer() != null && o.getCustomer().getName() != null)
                        ? o.getCustomer().getName()
                        : "Cliente";


        String orderNumber =
                (o.getOrderNumber() != null)
                        ? o.getOrderNumber()
                        : "#" + o.getId();

        List<ProductionCardItemDTO> items = new ArrayList<>();
        if (o.getItems() != null) {
            o.getItems().forEach(it -> {
                String name =
                        (it.getProduct() != null && it.getProduct().getName() != null)
                                ? it.getProduct().getName()
                                : "Item";

                Integer qty = (it.getQuantity() != null)
                        ? it.getQuantity()
                        : 1;

                items.add(new ProductionCardItemDTO(name, qty));
            });
        }

        return new ProductionCardDTO(
                o.getId(),
                orderNumber,
                customerName,
                po.getStage(),
                po.getStatus(),
                po.getNotes(),
                po.getUpdatedAt(),
                items
        );
    }
}
