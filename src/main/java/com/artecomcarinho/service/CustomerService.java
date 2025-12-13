package com.artecomcarinho.service;

import com.artecomcarinho.dto.CustomerKpiDTO;
import com.artecomcarinho.dto.CustomerSummaryDTO;
import com.artecomcarinho.model.Order;
import com.artecomcarinho.repository.CustomerRepository;
import com.artecomcarinho.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public List<CustomerSummaryDTO> getAllCustomersForAdmin() {
        return customerRepository.findAllByOrderByNameAsc()
                .stream()
                .map(c -> new CustomerSummaryDTO(
                        c.getId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getCreatedAt()
                ))
                .toList();
    }
    public List<CustomerKpiDTO> getCustomerKpis() {
        return orderRepository.getCustomerKpis();
    }
}

