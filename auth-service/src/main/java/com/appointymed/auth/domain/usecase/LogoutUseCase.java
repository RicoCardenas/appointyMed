package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.repository.RefreshTokenRepository;

public final class LogoutUseCase {
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(String value) {
        var token = refreshTokenRepository.findByToken(value);
        if (token.isPresent()) {
            token.get().revoke();
            refreshTokenRepository.save(token.get());
        }
    }
}
