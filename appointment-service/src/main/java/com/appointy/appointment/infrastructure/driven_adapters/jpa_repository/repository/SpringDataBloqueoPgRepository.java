package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository;

import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity.CitaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Funciones de bloqueo de PostgreSQL. Se declaran sobre CitaEntity solo porque Spring Data exige una entidad. */
public interface SpringDataBloqueoPgRepository extends JpaRepository<CitaEntity, UUID> {

    /** Candado de transaccion sobre una clave de texto; se libera solo al terminar la transaccion. */
    @Query(value = "select 1 from (select pg_advisory_xact_lock(hashtextextended(:clave, 0))) bloqueo", nativeQuery = true)
    int bloquearClave(@Param("clave") String clave);

    /** Espera maxima por un bloqueo dentro de esta transaccion; vencida, PostgreSQL aborta y la API responde 409. */
    @Query(value = "select set_config('lock_timeout', :milisegundos, true)", nativeQuery = true)
    String fijarEsperaMaxima(@Param("milisegundos") String milisegundos);
}
