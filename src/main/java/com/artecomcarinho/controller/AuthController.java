package com.artecomcarinho.controller;

import com.artecomcarinho.dto.AuthDTO;
import com.artecomcarinho.security.RateLimitService;
import com.artecomcarinho.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
@Tag(name = "Authentication", description = "Autenticacao e Registro")
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuario", description = "Cria uma nova conta de usuario")
    public ResponseEntity<AuthDTO.AuthResponse> register(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AuthDTO.RegisterRequest request) {
        rateLimitService.check("auth:register", rateLimitService.key(httpRequest, request.getEmail()), 5, Duration.ofHours(1));
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um usuario e retorna token JWT")
    public ResponseEntity<AuthDTO.AuthResponse> login(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AuthDTO.LoginRequest request) {
        rateLimitService.check("auth:login", rateLimitService.key(httpRequest, request.getEmail()), 5, Duration.ofMinutes(15));
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AuthDTO.ForgotPasswordRequest request) {
        rateLimitService.check("auth:forgot-password", rateLimitService.key(httpRequest, request.getEmail()), 3, Duration.ofHours(1));
        authService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            HttpServletRequest httpRequest,
            @Valid @RequestBody AuthDTO.ResetPasswordRequest request) {
        rateLimitService.check("auth:reset-password", rateLimitService.key(httpRequest, null), 5, Duration.ofMinutes(15));
        authService.updatePassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
