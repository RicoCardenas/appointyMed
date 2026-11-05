package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.HistorialCitaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataHistorialCitaRepository extends JpaRepository<HistorialCitaEntity, UUID> {

    List<HistorialCitaEntity> findByCitaIdOrderByFechaAsc(UUID citaId);
}
