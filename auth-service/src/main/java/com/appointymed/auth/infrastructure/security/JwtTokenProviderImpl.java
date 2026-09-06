package com.appointymed.auth.infrastructure.security;

import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
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

        List<String> roles = new java.util.ArrayList<>();
        for (int index = 0; index < user.getRoles().size(); index++) roles.add(user.getRoles().get(index).name());

        return Jwts.builder()
                .subject(user.getId().value())
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
    public DomainInstant getRefreshTokenExpiry() {
        return DomainInstant.ofEpochMillis(Instant.now().plusMillis(refreshTokenExpiryMs).toEpochMilli());
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

    public DomainId getUserIdFromAccessToken(String token) {
        return DomainId.of(validateAccessToken(token).getPayload().getSubject());
    }

    public DomainId getUserIdFromRefreshToken(String token) {
        return DomainId.of(validateRefreshToken(token).getPayload().getSubject());
    }

    public DomainList<String> getRolesFromAccessToken(String token) {
        Claims claims = validateAccessToken(token).getPayload();
        DomainList<String> result = new DomainList<>();
        List<?> roles = claims.get("roles", List.class);
        for (Object role : roles) result.add(String.valueOf(role));
        return result;
    }

    @Override
    public String generateToken(DomainId userId) {
        return Jwts.builder()
                .subject(userId.value())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(3600000))) // 1 hour expiry for password reset
                .signWith(accessTokenKey)
                .compact();
    }

    @Override
    public DomainInstant getExpiry() {
        return DomainInstant.ofEpochMillis(Instant.now().plusMillis(3600000).toEpochMilli()); // 1 hour
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
    public DomainId getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return DomainId.of(claims.getSubject());
    }
}
