package com.artecomcarinho.controller;

import com.artecomcarinho.dto.ProductDTO;
import com.artecomcarinho.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Public Products", description = "Endpoints públicos de produtos para a vitrine")
public class PublicProductController {

    private final ProductService productService;

    @GetMapping("/featured")
    @Operation(summary = "Listar produtos em destaque", description = "Retorna apenas os produtos marcados como destaque para a Home Page")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }
}