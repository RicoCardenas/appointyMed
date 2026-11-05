package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.HorarioAtencionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHorarioRepository extends JpaRepository<HorarioAtencionEntity, UUID> {

    List<HorarioAtencionEntity> findByProfesionalIdOrderByDiaSemanaAscHoraInicioAsc(UUID profesionalId);

    int countByProfesionalIdAndCentroId(UUID profesionalId, UUID centroId);
}
