package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ProfesionalEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProfesionalRepository extends JpaRepository<ProfesionalEntity, UUID> {

    ProfesionalEntity findByUsuarioId(UUID usuarioId);

    ProfesionalEntity findByRegistroMedico(String registroMedico);

    @Query(value = """
            select p.* from {h-schema}profesional p
            join {h-schema}profesional_especialidad pe on pe.profesional_id = p.id
            where pe.especialidad_id = :especialidadId and p.activo = true
            order by p.apellidos, p.nombres
            """, nativeQuery = true)
    List<ProfesionalEntity> listarActivosPorEspecialidad(@Param("especialidadId") UUID especialidadId);

    @Query(value = "select especialidad_id from {h-schema}profesional_especialidad where profesional_id = :profesionalId", nativeQuery = true)
    List<UUID> idsEspecialidades(@Param("profesionalId") UUID profesionalId);

    @Query(value = "select centro_id from {h-schema}profesional_centro where profesional_id = :profesionalId", nativeQuery = true)
    List<UUID> idsCentros(@Param("profesionalId") UUID profesionalId);

    @Modifying
    @Query(value = "delete from {h-schema}profesional_especialidad where profesional_id = :profesionalId", nativeQuery = true)
    void borrarEspecialidades(@Param("profesionalId") UUID profesionalId);

    @Modifying
    @Query(value = "insert into {h-schema}profesional_especialidad (profesional_id, especialidad_id) values (:profesionalId, :especialidadId)", nativeQuery = true)
    void insertarEspecialidad(@Param("profesionalId") UUID profesionalId, @Param("especialidadId") UUID especialidadId);

    @Modifying
    @Query(value = "delete from {h-schema}profesional_centro where profesional_id = :profesionalId", nativeQuery = true)
    void borrarCentros(@Param("profesionalId") UUID profesionalId);

    @Modifying
    @Query(value = "insert into {h-schema}profesional_centro (profesional_id, centro_id) values (:profesionalId, :centroId)", nativeQuery = true)
    void insertarCentro(@Param("profesionalId") UUID profesionalId, @Param("centroId") UUID centroId);

    @Query(value = "select exists (select 1 from {h-schema}profesional_centro where profesional_id = :profesionalId and centro_id = :centroId)", nativeQuery = true)
    boolean atiendeEnCentro(@Param("profesionalId") UUID profesionalId, @Param("centroId") UUID centroId);

    @Query(value = "select exists (select 1 from {h-schema}profesional_especialidad where profesional_id = :profesionalId and especialidad_id = :especialidadId)", nativeQuery = true)
    boolean tieneEspecialidad(@Param("profesionalId") UUID profesionalId, @Param("especialidadId") UUID especialidadId);
}
