package com.artecomcarinho.controller;

import com.artecomcarinho.dto.ProductionBoardDTO;
import com.artecomcarinho.dto.ProductionCardDTO;
import com.artecomcarinho.dto.UpdateProductionDTO;
import com.artecomcarinho.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProductionController {

    private final ProductionService productionService;

    @GetMapping("/board")
    public ProductionBoardDTO getBoard() {
        return productionService.getBoard();
    }

    @GetMapping("/orders/{orderId}")
    public ProductionCardDTO getOrCreate(@PathVariable Long orderId) {
        return productionService.ensureAndGet(orderId);
    }

    @PatchMapping("/orders/{orderId}")
    public ProductionCardDTO update(@PathVariable Long orderId,
                                    @RequestBody UpdateProductionDTO dto) {
        return productionService.update(orderId, dto);
    }

    @PostMapping("/orders/{orderId}/next")
    public ProductionCardDTO next(@PathVariable Long orderId) {
        return productionService.moveNext(orderId);
    }

    @PostMapping("/orders/{orderId}/prev")
    public ProductionCardDTO prev(@PathVariable Long orderId) {
        return productionService.movePrev(orderId);
    }
}
