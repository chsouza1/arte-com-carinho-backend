package com.artecomcarinho.controller;

import com.artecomcarinho.service.OrderPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class OrderPdfController {

    private final OrderPdfService orderPdfService;

    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> getOrderPdf(@PathVariable Long orderId) {
        byte[] pdf = orderPdfService.generateOrderPdf(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=pedido-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
