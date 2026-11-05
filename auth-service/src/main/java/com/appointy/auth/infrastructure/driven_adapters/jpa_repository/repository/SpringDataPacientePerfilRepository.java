package com.appointy.auth.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity.PacientePerfilEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPacientePerfilRepository extends JpaRepository<PacientePerfilEntity, UUID> {

    Optional<PacientePerfilEntity> findByCedula(String cedula);
}
