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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            log.debug("Resposta ja enviada. Nao e possivel redirecionar para {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        String baseUrl = (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl
                : "https://artecomcarinhobysi.com.br";

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String phoneAttribute = oAuth2User.getAttribute("phone");
            String phone = (phoneAttribute != null) ? phoneAttribute : "00000000000";

            if (email == null || email.isBlank()) {
                return UriComponentsBuilder.fromUriString(baseUrl + "/auth/login")
                        .queryParam("error", "email_not_provided")
                        .build()
                        .toUriString();
            }

            String normalizedEmail = email.trim().toLowerCase();

            User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .orElseGet(() -> {
                        log.debug("Criando novo usuario via social login");
                        String randomPassword = UUID.randomUUID().toString();

                        User newUser = User.builder()
                                .name(name != null ? name : "Usuario Social")
                                .email(normalizedEmail)
                                .phone(phone)
                                .password(passwordEncoder.encode(randomPassword))
                                .role(Role.CUSTOMER)
                                .active(true)
                                .build();
                        return userRepository.save(newUser);
                    });

            UserDTO userDTO = new UserDTO(user);
            String token = jwtUtil.generateToken(userDTO);

            return UriComponentsBuilder.fromUriString(baseUrl + "/auth/social-callback")
                    .fragment("token=" + token)
                    .build()
                    .toUriString();
        } catch (Exception e) {
            log.error("Erro critico no handler OAuth2", e);
            return UriComponentsBuilder.fromUriString(baseUrl + "/auth/login")
                    .queryParam("error", "internal_server_error")
                    .build()
                    .toUriString();
        }
    }
}
