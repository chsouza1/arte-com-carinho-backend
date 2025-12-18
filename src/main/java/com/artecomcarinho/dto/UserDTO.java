package com.artecomcarinho.dto;

import com.artecomcarinho.model.User.Role;
import com.artecomcarinho.model.User;
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

    @NotBlank(message = "Telefone e obrigatorio")
    @Size(min = 11, max = 12)
    private String phone;

    @NotNull(message = "Role é obrigatória")
    private Role role;

    private Boolean active;

    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.active = user.getActive();
        this.password = null;
    }
}