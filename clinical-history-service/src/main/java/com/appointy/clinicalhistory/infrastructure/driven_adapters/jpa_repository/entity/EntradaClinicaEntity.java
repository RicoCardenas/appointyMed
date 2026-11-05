package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Append-only (RF-29): {@code @Immutable} hace que Hibernate nunca emita un UPDATE; ademas la tabla tiene un trigger que lo impide. */
@Entity
@Immutable
@Table(name = "entrada_clinica")
@Getter
@Setter
@NoArgsConstructor
public class EntradaClinicaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Column(name = "motivo", nullable = false, columnDefinition = "text")
    private String motivo;

    @Column(name = "diagnostico", nullable = false, columnDefinition = "text")
    private String diagnostico;

    @Column(name = "notas", columnDefinition = "text")
    private String notas;

    @Column(name = "indicaciones", columnDefinition = "text")
    private String indicaciones;

    @Column(name = "corrige_a_id")
    private UUID corrigeAId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
