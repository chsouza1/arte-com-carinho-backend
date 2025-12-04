package com.artecomcarinho.config;

import com.artecomcarinho.model.User;
import com.artecomcarinho.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Se já existe admin, não cria outro
        if (userRepository.existsByEmail("admin@admin.com")) {
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email("admin@admin.com")
                .password(passwordEncoder.encode("Administrador.2025@!"))
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
    }
}
