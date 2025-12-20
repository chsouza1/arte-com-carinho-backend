package com.artecomcarinho.controller;

import com.artecomcarinho.dto.AddressDTO;
import com.artecomcarinho.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Gerenciamento de endereços de entrega")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Listar meus endereços")
    public ResponseEntity<List<AddressDTO>> getMyAddresses(Authentication authentication) {
        return ResponseEntity.ok(addressService.getUserAddresses(authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Adicionar novo endereço")
    public ResponseEntity<AddressDTO> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço")
    public ResponseEntity<AddressDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(addressService.updateAddress(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long id) {
        addressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}