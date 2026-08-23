package com.appointymed.auth.infrastructure.security;

import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.usecase.ForgotPasswordUseCase;
import com.appointymed.auth.domain.usecase.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider, ForgotPasswordUseCase.PasswordResetTokenProvider {
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtTokenProviderImpl(@Value("${app.jwt.access-token-secret}") String accessTokenSecret,
                            @Value("${app.jwt.refresh-token-secret}") String refreshTokenSecret,
                            @Value("${app.jwt.access-token-expiry-ms}") long accessTokenExpiryMs,
                            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshTokenExpiryMs) {
        this.accessTokenKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpiryMs);

        List<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail().getValue())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(accessTokenKey)
                .compact();
    }

    @Override
    public String generateRefreshToken() {
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(refreshTokenExpiryMs)))
                .signWith(refreshTokenKey)
                .compact();
    }

    @Override
    public Instant getRefreshTokenExpiry() {
        return Instant.now().plusMillis(refreshTokenExpiryMs);
    }

    public Jws<Claims> validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token);
    }

    public Jws<Claims> validateRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshTokenKey)
                .build()
                .parseSignedClaims(token);
    }

    public UUID getUserIdFromAccessToken(String token) {
        return UUID.fromString(validateAccessToken(token).getPayload().getSubject());
    }

    public UUID getUserIdFromRefreshToken(String token) {
        return UUID.fromString(validateRefreshToken(token).getPayload().getSubject());
    }

    public List<String> getRolesFromAccessToken(String token) {
        Claims claims = validateAccessToken(token).getPayload();
        return claims.get("roles", List.class);
    }

    @Override
    public String generateToken() {
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(3600000))) // 1 hour expiry for password reset
                .signWith(accessTokenKey)
                .compact();
    }

    @Override
    public Instant getExpiry() {
        return Instant.now().plusMillis(3600000); // 1 hour
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessTokenKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }
}