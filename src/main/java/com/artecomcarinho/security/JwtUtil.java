package com.artecomcarinho.security;

import com.artecomcarinho.dto.UserDTO;
import com.artecomcarinho.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final String ACCESS_PURPOSE = "access";
    private static final String PASSWORD_RESET_PURPOSE = "password_reset";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.password-reset-expiration:3600000}")
    private Long passwordResetExpiration;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("A configuracao jwt.secret e obrigatoria");
        }

        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("A configuracao jwt.secret deve ter pelo menos 32 bytes");
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", ACCESS_PURPOSE);
        return createToken(claims, userDetails.getUsername());
    }

    public String generatePasswordResetToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", PASSWORD_RESET_PURPOSE);
        return createToken(claims, userDetails.getUsername(), passwordResetExpiration);
    }

    public String generateToken(UserDTO userDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", ACCESS_PURPOSE);
        return createToken(claims, userDTO.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, expiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long tokenExpiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractPasswordResetUsername(String token) {
        Claims claims = extractAllClaims(token);
        validateTokenPurpose(claims, PASSWORD_RESET_PURPOSE);

        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new UnauthorizedException("Token de redefinicao de senha invalido");
        }

        return claims.getSubject();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        validateTokenPurpose(claims, ACCESS_PURPOSE);

        String username = claims.getSubject();
        Date tokenExpiration = claims.getExpiration();

        return username != null
                && username.equalsIgnoreCase(userDetails.getUsername())
                && tokenExpiration != null
                && !tokenExpiration.before(new Date());
    }

    private void validateTokenPurpose(Claims claims, String expectedPurpose) {
        Object purpose = claims.get("purpose");
        if (!expectedPurpose.equals(purpose)) {
            throw new UnauthorizedException("Token invalido");
        }
    }
}
