package com.artecomcarinho.controller;

import com.artecomcarinho.dto.ShippingQuoteOption;
import com.artecomcarinho.dto.ShippingQuoteRequest;
import com.artecomcarinho.model.Product;
import com.artecomcarinho.repository.ProductRepository;
import com.artecomcarinho.security.RateLimitService;
import com.artecomcarinho.service.MelhorEnvioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@ConditionalOnProperty(name = "melhorenvio.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/public/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final MelhorEnvioClient melhorEnvioClient;
    private final ProductRepository productRepository;
    private final RateLimitService rateLimitService;

    @PostMapping("/calculate")
    public List<ShippingQuoteOption> calculate(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ShippingQuoteRequest req) {
        rateLimitService.check("public:shipping", rateLimitService.key(httpRequest, req.getToZip()), 60, Duration.ofHours(1));

        if (req.getItems() != null) {
            for (ShippingQuoteRequest.Item item : req.getItems()) {
                if (item.getProductId() != null) {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        item.setPrice(product.getPrice().doubleValue());
                        item.setSku(product.getSku() != null ? product.getSku() : String.valueOf(product.getId()));
                    }
                }

                item.setWeight(0.3);
                item.setWidth(20);
                item.setHeight(15);
                item.setLength(20);

                if (item.getQty() == null || item.getQty() <= 0) {
                    item.setQty(1);
                }
            }
        }

        return melhorEnvioClient.quote(req);
    }
}
