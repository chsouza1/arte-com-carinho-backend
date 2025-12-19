package com.artecomcarinho.dto;

import com.artecomcarinho.model.User;
import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Nome é obrigatório")
        private String name;

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Numero de telefone é obrigatória")
        @Size(min = 11, max = 12, message = "esse telefone deve conter")
        private String phone;

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter pelo menos 6 caracteres")
        private String password;

        @NotNull(message = "Role é obrigatória")
        private User.Role role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        private String password;

        private String captchaToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {

        private String token;
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String role;
        private Boolean active;
    }
}
