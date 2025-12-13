package com.artecomcarinho.controller;

import com.artecomcarinho.dto.ShippingQuoteOption;
import com.artecomcarinho.dto.ShippingQuoteRequest;
import com.artecomcarinho.service.MelhorEnvioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final MelhorEnvioClient melhorEnvioClient;

    @PostMapping("/quote")
    public List<ShippingQuoteOption> quote(@RequestBody ShippingQuoteRequest req) {
        return melhorEnvioClient.quote(req);
    }
}

