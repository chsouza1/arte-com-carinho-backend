package com.artecomcarinho.security;

import com.artecomcarinho.dto.UserDTO;
import com.artecomcarinho.model.User;
import com.artecomcarinho.model.User.Role;
import com.artecomcarinho.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extrair dados do provedor (Google/Facebook retornam 'email' e 'name')
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            response.sendRedirect(frontendUrl + "/auth/login?error=email_not_provided");
            return;
        }

        // Buscar ou Criar Usuário
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name(name != null ? name : "Usuário")
                            .email(email)
                            .password("")
                            .role(Role.CUSTOMER)
                            .active(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Gerar Token JWT
        UserDTO userDTO = new UserDTO(user);
        String token = jwtUtil.generateToken(userDTO);

        // Redirecionar para o Frontend com o token
        String targetUrl = String.format("%s/auth/callback?token=%s&name=%s&email=%s&role=%s",
                frontendUrl, token, user.getName(), user.getEmail(), user.getRole());

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}