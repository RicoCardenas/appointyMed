package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.TokenExpiredException;
import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.UserNotFoundException;

import java.time.Instant;
import java.util.UUID;

@Component
public class RefreshAccessTokenUseCase {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final com.appointymed.auth.domain.usecase.JwtTokenProvider jwtTokenProvider;

    public RefreshAccessTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                                     UserRepository userRepository,
                                     com.appointymed.auth.domain.usecase.JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public RefreshTokenResult execute(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new TokenExpiredException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.deleteById(refreshToken.getId());
            throw new TokenExpiredException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(refreshToken.getUserId()));

        if (user.getStatus() != com.appointymed.auth.domain.model.UserStatus.ACTIVE) {
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken();
        Instant newRefreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiry();

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .userId(user.getId())
                .expiresAt(newRefreshTokenExpiry)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResult(newAccessToken, newRefreshTokenValue);
    }

    public record RefreshTokenResult(String accessToken, String refreshToken) {}
}