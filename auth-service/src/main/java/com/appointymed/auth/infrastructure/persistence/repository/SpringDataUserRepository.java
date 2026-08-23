package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.status = ?1")
    Page<UserJpaEntity> findByStatus(UserJpaEntity.UserStatus status, Pageable pageable);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.status = ?1")
    List<UserJpaEntity> findByStatus(UserJpaEntity.UserStatus status);
}