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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Resposta já enviada. Não é possível redirecionar para " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String baseUrl = (frontendUrl != null && !frontendUrl.isEmpty()) ? frontendUrl : "http://localhost:3000";

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String phone = oAuth2User.getAttribute("phone");

            if (email == null) {
                return UriComponentsBuilder.fromUriString(baseUrl + "/auth/login")
                        .queryParam("error", "email_not_provided")
                        .build().toUriString();
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Criando novo usuário via Social Login: {}", email);
                        // Senha aleatória forte para segurança
                        String randomPassword = UUID.randomUUID().toString();

                        User newUser = User.builder()
                                .name(name != null ? name : "Usuário Social")
                                .email(email)
                                .phone(phone)
                                .password(passwordEncoder.encode(randomPassword))
                                .role(Role.CUSTOMER) // Define como Cliente padrão
                                .active(true)
                                .build();
                        return userRepository.save(newUser);
                    });

            // Gera o Token JWT
            UserDTO userDTO = new UserDTO(user);
            String token = jwtUtil.generateToken(userDTO);

            return UriComponentsBuilder.fromUriString(baseUrl + "/auth/social-callback")
                    .queryParam("token", token)
                    .build().toUriString();

        } catch (Exception e) {
            log.error("Erro no processamento do OAuth2 Success", e);
            return UriComponentsBuilder.fromUriString(baseUrl + "/auth/login")
                    .queryParam("error", "internal_server_error")
                    .build().toUriString();
        }
    }

    // Limpa os cookies de autenticação para não dar conflito no próximo login
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}