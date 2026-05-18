package com.artecomcarinho.controller;

import com.artecomcarinho.model.Material;
import com.artecomcarinho.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    @Autowired
    private MaterialRepository materialRepository;

    @GetMapping
    public ResponseEntity<List<Material>> getAllMaterials() {
        return ResponseEntity.ok(materialRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        if (material.getStockQuantity() == null) material.setStockQuantity(0);
        return ResponseEntity.ok(materialRepository.save(material));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Material> updateStock(@PathVariable Long id, @RequestParam Integer newStock) {
        return materialRepository.findById(id).map(material -> {
            material.setStockQuantity(newStock);
            return ResponseEntity.ok(materialRepository.save(material));
        }).orElse(ResponseEntity.notFound().build());
    }
}