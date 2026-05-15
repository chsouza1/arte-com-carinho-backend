package com.artecomcarinho.config;

import com.artecomcarinho.model.User;
import com.artecomcarinho.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.email:}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapAdminPassword;

    @Value("${app.bootstrap-admin.phone:}")
    private String bootstrapAdminPhone;

    @Override
    public void run(String... args) {
        if (!bootstrapAdminEnabled
                || bootstrapAdminEmail == null || bootstrapAdminEmail.isBlank()
                || bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()
                || bootstrapAdminPhone == null || bootstrapAdminPhone.isBlank()) {
            return;
        }

        if (bootstrapAdminPassword.length() < 12) {
            throw new IllegalStateException("A senha do bootstrap admin deve ter pelo menos 12 caracteres");
        }

        String normalizedEmail = bootstrapAdminEmail.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email(normalizedEmail)
                .phone(bootstrapAdminPhone)
                .password(passwordEncoder.encode(bootstrapAdminPassword))
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
    }
}
