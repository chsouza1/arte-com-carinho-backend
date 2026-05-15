package com.artecomcarinho.service;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.dto.PublicOrderRequest;
import com.artecomcarinho.exception.InvalidOperationException;
import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicOrderService {

    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final TurnstileService turnstileService;

    public OrderDTO createPublicOrder(PublicOrderRequest request) {
        if (!turnstileService.validateToken(request.getCaptchaToken())) {
            throw new InvalidOperationException("Verificacao de seguranca falhou");
        }

        String normalizedEmail = request.getCustomer().getEmail().trim().toLowerCase();

        Customer customer = customerRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setName(request.getCustomer().getName());
                    c.setEmail(normalizedEmail);
                    try {
                        Customer.class.getDeclaredField("phone");
                        c.setPhone(request.getCustomer().getPhone());
                    } catch (NoSuchFieldException ex) {
                        // Campo opcional na entidade.
                    }
                    return customerRepository.save(c);
                });

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCustomerId(customer.getId());
        orderDTO.setOrderDate(LocalDateTime.now());
        orderDTO.setExpectedDeliveryDate(null);
        orderDTO.setNotes(request.getNotes());
        orderDTO.setCustomizationDetails(null);
        orderDTO.setPaymentMethod(mapPaymentMethod(request.getPaymentMethod()));
        orderDTO.setPaymentStatus(Order.PaymentStatus.PENDING);
        orderDTO.setDiscount(null);
        orderDTO.setShippingCost(request.getShippingCost() != null ? request.getShippingCost() : java.math.BigDecimal.ZERO);
        orderDTO.setItems(
                request.getItems().stream().map(itemReq -> {
                    OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                    itemDTO.setProductId(itemReq.getProductId());
                    itemDTO.setQuantity(itemReq.getQuantity());
                    itemDTO.setSelectedSize(itemReq.getSelectedSize());
                    itemDTO.setSelectedColor(itemReq.getSelectedColor());
                    itemDTO.setCustomizationNotes(itemReq.getCustomizationNotes());
                    return itemDTO;
                }).collect(Collectors.toList())
        );

        return orderService.createOrder(orderDTO);
    }

    private Order.PaymentMethod mapPaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim().toLowerCase()
                .replace("ã", "a")
                .replace("á", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("ú", "u")
                .replace("ç", "c")
                .replaceAll("\\s+", " ");

        return switch (normalized) {
            case "pix", "chave pix", "pagar com pix" -> Order.PaymentMethod.PIX;
            case "cartao", "cartao credito", "cartao debito", "credito", "debito",
                    "cartao de credito", "cartao de debito" -> Order.PaymentMethod.CARD;
            case "dinheiro", "especie", "em especie", "pagar em dinheiro" -> Order.PaymentMethod.CASH;
            default -> Order.PaymentMethod.PIX;
        };
    }
}
