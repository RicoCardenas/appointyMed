package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
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
    public DomainOptional<RefreshToken> findByToken(String token) {
        Optional<RefreshTokenJpaEntity> entity = springDataRefreshTokenRepository.findByToken(token);
        return entity.isPresent() ? DomainOptional.of(RefreshTokenPersistenceMapper.toDomain(entity.get())) : DomainOptional.empty();
    }

    @Override
    public DomainList<RefreshToken> findByUserId(DomainId userId) {
        DomainList<RefreshToken> result = new DomainList<>();
        for (RefreshTokenJpaEntity entity : springDataRefreshTokenRepository.findByUserId(UUID.fromString(userId.value()))) result.add(RefreshTokenPersistenceMapper.toDomain(entity));
        return result;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = RefreshTokenPersistenceMapper.toEntity(refreshToken);
        RefreshTokenJpaEntity saved = springDataRefreshTokenRepository.save(entity);
        return RefreshTokenPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(DomainId id) {
        springDataRefreshTokenRepository.deleteById(UUID.fromString(id.value()));
    }

    @Override
    public void deleteByUserId(DomainId userId) {
        springDataRefreshTokenRepository.deleteByUserId(UUID.fromString(userId.value()));
    }

    @Override
    public void deleteExpiredTokens(DomainInstant now) {
        springDataRefreshTokenRepository.deleteExpiredTokens(Instant.ofEpochMilli(now.toEpochMillis()));
    }

    @Override
    public long countByUserId(DomainId userId) {
        return springDataRefreshTokenRepository.countByUserId(UUID.fromString(userId.value()));
    }
}
