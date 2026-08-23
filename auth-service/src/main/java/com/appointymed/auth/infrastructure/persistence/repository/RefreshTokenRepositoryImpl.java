package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.appointymed.auth.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;

    public RefreshTokenRepositoryImpl(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return springDataRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(UUID userId) {
        return springDataRefreshTokenRepository.findByUserId(userId).stream()
                .map(RefreshTokenPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenPersistenceMapper.toEntity(refreshToken);
        RefreshTokenJpaEntity saved = springDataRefreshTokenRepository.save(entity);
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRefreshTokenRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataRefreshTokenRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteExpiredTokens() {
        springDataRefreshTokenRepository.deleteExpiredTokens(Instant.now());
    }

    @Override
    public long countByUserId(UUID userId) {
        return springDataRefreshTokenRepository.countByUserId(userId);
    }
}