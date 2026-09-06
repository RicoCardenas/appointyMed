package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.TokenExpiredException;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;

public final class RefreshAccessTokenUseCase {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainClock clock;

    public RefreshAccessTokenUseCase(
            RefreshTokenRepository r, UserRepository u, JwtTokenProvider j, DomainClock c) {
        refreshTokenRepository = r;
        userRepository = u;
        jwtTokenProvider = j;
        clock = c;
    }

    public RefreshTokenResult execute(String value) {
        RefreshToken token =
                refreshTokenRepository
                        .findByToken(value)
                        .orElseThrow(() -> new TokenExpiredException("Invalid refresh token"));
        if (!token.isValid(clock.now())) {
            if (token.getId() != null) {
                refreshTokenRepository.deleteById(token.getId());
            }
            throw new TokenExpiredException("Refresh token has expired or been revoked");
        }
        User user =
                userRepository
                        .findById(token.getUserId())
                        .orElseThrow(() -> new UserNotFoundException(token.getUserId()));
        if (user.getStatus() != UserStatus.ACTIVE)
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        String access = jwtTokenProvider.generateAccessToken(user),
                refresh = jwtTokenProvider.generateRefreshToken();
        DomainInstant expiry = jwtTokenProvider.getRefreshTokenExpiry();
        token.revoke();
        refreshTokenRepository.save(token);
        refreshTokenRepository.save(
                RefreshToken.builder().token(refresh).userId(user.getId()).expiresAt(expiry).build());
        return new RefreshTokenResult(access, refresh);
    }

    public record RefreshTokenResult(String accessToken, String refreshToken) {}
}
