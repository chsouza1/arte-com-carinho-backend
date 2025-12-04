package com.artecomcarinho.dto;

import com.artecomcarinho.model.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotNull(message = "Role é obrigatória")
    private Role role;

    private Boolean active;

    // Usado apenas para criação/atualização
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;
}