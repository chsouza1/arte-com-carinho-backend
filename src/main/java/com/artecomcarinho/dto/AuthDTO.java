package com.artecomcarinho.dto;

import com.artecomcarinho.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Nome e obrigatorio")
        private String name;

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        private String email;

        @NotBlank(message = "Numero de telefone e obrigatorio")
        @Size(min = 11, max = 12, message = "Telefone deve ter entre 11 e 12 digitos")
        private String phone;

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
        private String password;

        private User.Role role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        private String email;

        @NotBlank(message = "Senha e obrigatoria")
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "O token e obrigatorio")
        private String token;

        @NotBlank(message = "A nova senha e obrigatoria")
        @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
        private String newPassword;
    }
}
