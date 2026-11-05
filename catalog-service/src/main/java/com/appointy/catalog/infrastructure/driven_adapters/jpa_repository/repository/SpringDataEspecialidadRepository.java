package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.EspecialidadEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEspecialidadRepository extends JpaRepository<EspecialidadEntity, UUID> {

    EspecialidadEntity findByNombre(String nombre);

    List<EspecialidadEntity> findAllByOrderByNombre();

    List<EspecialidadEntity> findByActivoTrueOrderByNombre();
}
