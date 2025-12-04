package com.artecomcarinho.service;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.dto.PublicOrderRequest;
import com.artecomcarinho.model.Customer;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicOrderService {

    private final CustomerRepository customerRepository;
    private final OrderService orderService;

    /**
     * Cria um pedido público (site) a partir dos dados de cliente + item único ou múltiplos.
     */
    public OrderDTO createPublicOrder(PublicOrderRequest request) {
        // 1) Encontrar ou criar cliente pelo e-mail
        Customer customer = customerRepository
                .findByEmailIgnoreCase(request.getCustomer().getEmail())
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setName(request.getCustomer().getName());
                    c.setEmail(request.getCustomer().getEmail());
                    // se tiver campo phone no Customer, set aqui
                    try {
                        Customer.class.getDeclaredField("phone");
                        c.setPhone(request.getCustomer().getPhone());
                    } catch (NoSuchFieldException ex) {
                        // se a entidade não tiver phone, ignora
                    }
                    return customerRepository.save(c);
                });

        // 2) Montar OrderDTO para reaproveitar OrderService.createOrder
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCustomerId(customer.getId());
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setExpectedDeliveryDate(null);
        orderDTO.setNotes(request.getNotes());
        orderDTO.setCustomizationDetails(null);
        orderDTO.setPaymentMethod(mapPaymentMethod(request.getPaymentMethod()));
        orderDTO.setPaymentStatus(Order.PaymentStatus.PENDING); // ajusta se o enum for outro nome
        orderDTO.setDiscount(null); // sem desconto por padrão;
        orderDTO.setItems(
                request.getItems().stream().map(itemReq -> {
                    OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                    itemDTO.setProductId(itemReq.getProductId());
                    itemDTO.setQuantity(itemReq.getQuantity());
                    itemDTO.setSelectedSize(itemReq.getSelectedSize());
                    itemDTO.setSelectedColor(itemReq.getSelectedColor());
                    itemDTO.setCustomizationNotes(itemReq.getCustomizationNotes());
                    // unitPrice e subtotal serão tratados em OrderService com base no Product
                    return itemDTO;
                }).collect(Collectors.toList())
        );

        // 4) Delega para o fluxo normal de criação de pedido
        return orderService.createOrder(orderDTO);
    }

    private Order.PaymentMethod mapPaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        // normaliza: minúsculo, sem espaços extras
        String normalized = raw.trim().toLowerCase();

        // troca caracteres com acento em versões simples
        normalized = normalized
                .replace("ã", "a")
                .replace("á", "a")
                .replace("â", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("ú", "u")
                .replace("ç", "c");

        // remove espaços duplos
        normalized = normalized.replaceAll("\\s+", " ");

        switch (normalized) {
            // ==================
            // PIX
            // ==================
            case "pix":
            case "chave pix":
            case "pagar com pix":
                return Order.PaymentMethod.PIX;

            // ==================
            // CARTÃO (débito/crédito)
            // ==================
            case "cartao":
            case "cartao credito":
            case "cartao debito":
            case "credito":
            case "debito":
            case "cartao de credito":
            case "cartao de debito":
                return Order.PaymentMethod.CARD;

            // ==================
            // DINHEIRO
            // ==================
            case "dinheiro":
            case "especie":
            case "em especie":
            case "pagar em dinheiro":
                return Order.PaymentMethod.CASH;

            default:
                return Order.PaymentMethod.PIX;
        }
    }

}
