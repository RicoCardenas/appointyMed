package com.appointymed.appointment.infrastructure.persistence.repository;

import com.appointymed.appointment.infrastructure.persistence.entity.SpecialtyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSpecialtyRepository extends JpaRepository<SpecialtyJpaEntity, UUID> {
    Optional<SpecialtyJpaEntity> findByNameIgnoreCase(String name);
    List<SpecialtyJpaEntity> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
