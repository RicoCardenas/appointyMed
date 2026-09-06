package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
import com.appointymed.auth.domain.model.RefreshToken;

public interface RefreshTokenRepository {
    DomainOptional<RefreshToken> findByToken(String token);

    DomainList<RefreshToken> findByUserId(DomainId userId);

    RefreshToken save(RefreshToken refreshToken);

    void deleteById(DomainId id);

    void deleteByUserId(DomainId userId);

    void deleteExpiredTokens(DomainInstant now);

    long countByUserId(DomainId userId);
}
