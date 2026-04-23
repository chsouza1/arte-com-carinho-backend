package com.artecomcarinho.controller;

import com.artecomcarinho.dto.ShippingQuoteOption;
import com.artecomcarinho.dto.ShippingQuoteRequest;
import com.artecomcarinho.model.Product;
import com.artecomcarinho.repository.ProductRepository;
import com.artecomcarinho.service.MelhorEnvioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@ConditionalOnProperty(name = "melhorenvio.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/public/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final MelhorEnvioClient melhorEnvioClient;
    private final ProductRepository productRepository;

    @PostMapping("/calculate")
    public List<ShippingQuoteOption> calculate(@RequestBody ShippingQuoteRequest req) {

        if (req.getItems() != null) {
            for (ShippingQuoteRequest.Item item : req.getItems()) {


                if (item.getProductId() != null) {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        item.setPrice(product.getPrice().doubleValue());
                        item.setSku(product.getSku() != null ? product.getSku() : String.valueOf(product.getId()));
                    }
                }

                // 2. MEDIDAS PADRÃO PARA CADA ITEM
                item.setWeight(0.3); // 300 gramas
                item.setWidth(20);   // 20 cm
                item.setHeight(15);  // 15 cm
                item.setLength(20);  // 20 cm

                if (item.getQty() == null || item.getQty() <= 0) {
                    item.setQty(1);
                }
            }
        }

        return melhorEnvioClient.quote(req);
    }
}