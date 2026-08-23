package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.exception.TokenExpiredException;

@Component
public class LogoutUseCase {
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                });
    }
}