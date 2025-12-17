package com.artecomcarinho.config;

import com.artecomcarinho.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.artecomcarinho.security.JwtAuthenticationFilter;
import com.artecomcarinho.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setDefaultFailureUrl("https://www.artecomcarinhobysi.com.br/auth/login?error=social_login_failed");

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())


                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))


                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        // Libera todas as variantes possíveis das rotas de OAuth2 para evitar bloqueio
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/api/oauth2/**", "/api/login/oauth2/**").permitAll()

                        // Rotas Auth
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()

                        // Rotas Públicas
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/products/**").permitAll()
                        .requestMatchers("/api/public/**", "/public/**").permitAll()

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Admin
                        .requestMatchers("/api/products/**", "/api/users/**").hasRole("ADMIN")


                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/api/oauth2/authorization")
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(red -> red
                                .baseUri("/api/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(failureHandler)
                )
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}