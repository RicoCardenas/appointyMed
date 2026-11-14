package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.EntradaClinicaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEntradaClinicaRepository extends JpaRepository<EntradaClinicaEntity, UUID> {

    List<EntradaClinicaEntity> findByPacienteIdOrderByCreadoEnAscIdAsc(UUID pacienteId);

    Page<EntradaClinicaEntity> findByProfesionalId(UUID profesionalId, Pageable pageable);

    boolean existsByCitaIdAndCorrigeAIdIsNull(UUID citaId);
}
