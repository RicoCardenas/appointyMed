package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.CentroEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCentroRepository extends JpaRepository<CentroEntity, UUID> {

    @Query(value = """
            select c.* from {h-schema}centro c
            join {h-schema}profesional_centro pc on pc.centro_id = c.id
            where pc.profesional_id = :profesionalId and c.activo = true
            order by c.nombre
            """, nativeQuery = true)
    List<CentroEntity> listarActivosDeProfesional(@Param("profesionalId") UUID profesionalId);
}
