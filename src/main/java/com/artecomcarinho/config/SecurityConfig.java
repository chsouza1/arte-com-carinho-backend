package com.artecomcarinho.config;

import com.artecomcarinho.security.JwtAuthenticationFilter;
import com.artecomcarinho.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                // O Spring precisa de sessão temporária durante o redirect do OAuth2
                // (Se deixar totalmente STATELESS aqui, às vezes dá erro "authorization_request_not_found")
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // --- CORREÇÃO AQUI: Liberar as rotas de login social ---
                        .requestMatchers("/api/oauth2/**").permitAll()       // Libera o início do login
                        .requestMatchers("/api/login/oauth2/code/**").permitAll() // Libera o callback do Google
                        // -----------------------------------------------------

                        // Rotas de autenticação padrão
                        .requestMatchers("/api/auth/**").permitAll()

                        // Rotas públicas
                        .requestMatchers("/api/public/**").permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Produtos
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/products/**").hasRole("ADMIN")

                        // Usuários
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Pedidos
                        .requestMatchers("/api/orders/**").authenticated()

                        // Resto bloqueado
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth.baseUri("/api/oauth2/authorization"))
                        .redirectionEndpoint(red -> red.baseUri("/api/login/oauth2/code/*"))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}