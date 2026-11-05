package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.FranjaEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataFranjaRepository extends JpaRepository<FranjaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FranjaEntity f where f.id = :id")
    Optional<FranjaEntity> buscarParaActualizar(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FranjaEntity f where f.profesionalId = :profesionalId and f.centroId = :centroId "
            + "and f.fecha between :desde and :hasta order by f.id")
    List<FranjaEntity> bloquearRango(@Param("profesionalId") UUID profesionalId, @Param("centroId") UUID centroId,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    List<FranjaEntity> findByProfesionalIdAndCentroIdAndFechaAndEstadoOrderByHoraInicio(
            UUID profesionalId, UUID centroId, LocalDate fecha, EstadoFranja estado);

    @Query("select f from FranjaEntity f where f.fecha = :fecha and f.id in "
            + "(select c.franjaId from CitaEntity c where c.pacienteId = :pacienteId and c.estado = :estado)")
    List<FranjaEntity> franjasDeCitasDelPaciente(
            @Param("pacienteId") UUID pacienteId, @Param("fecha") LocalDate fecha, @Param("estado") EstadoCita estado);

    List<FranjaEntity> findByIdIn(Collection<UUID> ids);

    /** Idempotente por la restriccion uq_franja_horario; devuelve las filas insertadas (0 o 1). */
    @Modifying
    @Query(value = "insert into franja (id, profesional_id, centro_id, fecha, hora_inicio, hora_fin, estado, version) "
            + "values (:id, :profesionalId, :centroId, :fecha, :horaInicio, :horaFin, 'DISPONIBLE', 0) "
            + "on conflict (profesional_id, centro_id, fecha, hora_inicio) do nothing", nativeQuery = true)
    int insertarSiNoExiste(@Param("id") UUID id, @Param("profesionalId") UUID profesionalId, @Param("centroId") UUID centroId,
            @Param("fecha") LocalDate fecha, @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);
}
