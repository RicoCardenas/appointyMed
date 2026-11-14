package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.CitaEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCitaRepository extends JpaRepository<CitaEntity, UUID>, JpaSpecificationExecutor<CitaEntity> {

    @Query("select c from CitaEntity c, FranjaEntity f where f.id = c.franjaId and c.pacienteId = :pacienteId "
            + "and f.fecha between :desde and :hasta order by f.fecha, f.horaInicio")
    List<CitaEntity> listarDePaciente(@Param("pacienteId") UUID pacienteId, @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select c from CitaEntity c, FranjaEntity f where f.id = c.franjaId and c.profesionalId = :profesionalId "
            + "and c.estado = :estado and f.fecha between :desde and :hasta order by f.fecha, f.horaInicio")
    List<CitaEntity> listarDeProfesional(@Param("profesionalId") UUID profesionalId, @Param("estado") EstadoCita estado,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select c.id from CitaEntity c, FranjaEntity f where f.id = c.franjaId and c.profesionalId = :profesionalId "
            + "and f.centroId = :centroId and c.estado = :estado and f.fecha between :desde and :hasta order by c.id")
    List<UUID> idsEnRango(@Param("profesionalId") UUID profesionalId, @Param("centroId") UUID centroId, @Param("estado") EstadoCita estado,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    long countByEstado(EstadoCita estado);

    boolean existsByProfesionalIdAndPacienteIdAndEstadoIn(UUID profesionalId, UUID pacienteId, Collection<EstadoCita> estados);
}
