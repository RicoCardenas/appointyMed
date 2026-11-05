package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.BloqueoEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataBloqueoRepository extends JpaRepository<BloqueoEntity, UUID> {

    int countByProfesionalIdAndCentroId(UUID profesionalId, UUID centroId);
}
