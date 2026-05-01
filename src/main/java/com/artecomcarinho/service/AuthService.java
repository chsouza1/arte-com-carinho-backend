package com.artecomcarinho.service;

import com.artecomcarinho.dto.AuthDTO;
import com.artecomcarinho.exception.DuplicateResourceException;
import com.artecomcarinho.model.User;
import com.artecomcarinho.repository.UserRepository;
import com.artecomcarinho.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TurnstileService turnstileService;
    private final NotificationService notificationService;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
            throw new DuplicateResourceException("Ja existe um usuario com esse e-mail");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        boolean isCaptchaValid = turnstileService.validateToken(request.getCaptchaToken());
        if (!isCaptchaValid) {
            throw new BadCredentialsException("Verificacao de seguranca falhou");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadCredentialsException("Usuario inativo");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais invalidas");
        }

        String token = jwtUtil.generateToken(user);

        return AuthDTO.AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }

    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String token = jwtUtil.generatePasswordResetToken(user);
        notificationService.sendPasswordResetEmail(user, token);
    }

    @Transactional
    public void updatePassword(String token, String newPassword) {
        String email = jwtUtil.extractPasswordResetUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
