package com.artecomcarinho.security;

import com.artecomcarinho.dto.UserDTO;
import com.artecomcarinho.model.User;
import com.artecomcarinho.model.User.Role;
import com.artecomcarinho.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Logs para depuração
            log.info("OAuth2 Login Sucesso. Atributos: {}", oAuth2User.getAttributes());

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String phone = oAuth2User.getAttribute("phone");

            if (email == null) {
                log.error("Email não retornado pelo provedor");
                response.sendRedirect(frontendUrl + "/auth/login?error=email_not_provided");
                return;
            }

            // Buscar ou Criar Usuário
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Criando novo usuário via Social Login: {}", email);
                        // Cria uma senha aleatória forte para satisfazer validações do banco
                        String randomPassword = UUID.randomUUID().toString();

                        User newUser = User.builder()
                                .name(name != null ? name : "Usuário Social")
                                .email(email)
                                .phone(phone)
                                .password(passwordEncoder.encode(randomPassword))
                                .role(Role.CUSTOMER)
                                .active(true)
                                .build();
                        return userRepository.save(newUser);
                    });

            // Gerar Token JWT
            UserDTO userDTO = new UserDTO(user);
            String token = jwtUtil.generateToken(userDTO);

            log.info("Token gerado com sucesso para {}", email);

            // Redirecionar para o Frontend com o token
            String targetUrl = String.format("%s/auth/callback?token=%s&name=%s&email=%s&role=%s",
                    frontendUrl, token, user.getName(), user.getEmail(), user.getRole());

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("Erro crítico no handler OAuth2", e);
            // Redireciona para o front com erro genérico
            response.sendRedirect(frontendUrl + "/auth/login?error=internal_server_error");
        }
    }
}