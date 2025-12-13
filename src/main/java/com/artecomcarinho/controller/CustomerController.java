// CustomerController.java
package com.artecomcarinho.controller;

import com.artecomcarinho.dto.CustomerKpiDTO;
import com.artecomcarinho.dto.CustomerSummaryDTO;
import com.artecomcarinho.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/admin")
    public List<CustomerSummaryDTO> getAllCustomersForAdmin() {
        return customerService.getAllCustomersForAdmin();
    }

    @GetMapping("/admin/kpis")
    public List<CustomerKpiDTO> getCustomerKpis() {
        return customerService.getCustomerKpis();
    }
}
