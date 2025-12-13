package com.artecomcarinho.service;

import com.artecomcarinho.dto.OrderDTO;
import com.artecomcarinho.exception.ResourceNotFoundException;
import com.artecomcarinho.model.*;
import com.artecomcarinho.model.Order.OrderStatus;
import com.artecomcarinho.repository.CustomerRepository;
import com.artecomcarinho.repository.OrderRepository;
import com.artecomcarinho.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.artecomcarinho.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
        return convertToDTO(order);
    }

    public OrderDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com número: " + orderNumber));
        return convertToDTO(order);
    }

    public Page<OrderDTO> getOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    public Page<OrderDTO> getOrdersByCustomerEmail(String email, Pageable pageable) {

        return customerRepository.findByEmailIgnoreCase(email)
                .map(customer ->
                        orderRepository
                                .findByCustomerId(customer.getId(), pageable)
                                .map(this::convertToDTO)
                )
                .orElse(Page.empty(pageable));
    }


    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }

    public List<OrderDTO> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getUpcomingDeliveries(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findUpcomingDeliveries(startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getTotalRevenueBetweenDates(startDate, endDate);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        // Validar cliente
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + orderDTO.getCustomerId()));

        // Criar pedido
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setStatus(orderDTO.getStatus() != null ? orderDTO.getStatus() : OrderStatus.PENDING);
        order.setOrderDate(orderDTO.getOrderDate() != null ? orderDTO.getOrderDate() : LocalDate.now());
        order.setExpectedDeliveryDate(orderDTO.getExpectedDeliveryDate());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(orderDTO.getPaymentStatus());
        order.setNotes(orderDTO.getNotes());
        order.setCustomizationDetails(orderDTO.getCustomizationDetails());
        order.setDiscount(orderDTO.getDiscount() != null ? orderDTO.getDiscount() : BigDecimal.ZERO);

        // Adicionar itens
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + itemDTO.getProductId()));

            // Verificar estoque
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : product.getPrice());
            item.setSelectedSize(itemDTO.getSelectedSize());
            item.setSelectedColor(itemDTO.getSelectedColor());
            item.setCustomizationNotes(itemDTO.getCustomizationNotes());
            item.calculateSubtotal();

            order.getItems().add(item);
            total = total.add(item.getSubtotal());

            // Atualizar estoque
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);

        }

        order.setTotalAmount(total.subtract(order.getDiscount()));

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);


        if (newStatus == OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
            order.setDeliveredDate(LocalDate.now());
        }

        Order updatedOrder = orderRepository.save(order);

        try {
            notificationService.notifyOrderStatusChange(updatedOrder, oldStatus, newStatus);
        } catch(Exception e) {
            System.out.println("Falha ao enviar notificação de mudança de status: " + e.getMessage());
        }

        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Não é possível cancelar um pedido já entregue");
        }

        // Devolver estoque
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("PED-%s-%04d", date, count);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getName());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscount(order.getDiscount());
        dto.setOrderDate(order.getOrderDate());
        dto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        dto.setDeliveredDate(order.getDeliveredDate());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setNotes(order.getNotes());
        dto.setCustomizationDetails(order.getCustomizationDetails());

        List<OrderDTO.OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private OrderDTO.OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderDTO.OrderItemDTO dto = new OrderDTO.OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setSelectedSize(item.getSelectedSize());
        dto.setSelectedColor(item.getSelectedColor());
        dto.setCustomizationNotes(item.getCustomizationNotes());
        return dto;
    }
}