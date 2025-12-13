package com.artecomcarinho.controller;

import com.artecomcarinho.service.ProductionPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProductionPdfController {

    private final ProductionPdfService productionPdfService;

    @GetMapping("/orders/{orderId}/pdf")
    public ResponseEntity<byte[]> getProductionPdf(@PathVariable Long orderId) {
        byte[] pdf = productionPdfService.generateProductionPdf(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=producao-pedido-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
