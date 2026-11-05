package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Append-only (RF-30). */
@Entity
@Immutable
@Table(name = "log_acceso_clinico")
@Getter
@Setter
@NoArgsConstructor
public class LogAccesoEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "rol", nullable = false)
    private String rol;

    @Column(name = "paciente_id")
    private UUID pacienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private AccionAcceso accion;

    @Column(name = "entrada_id")
    private UUID entradaId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false)
    private ResultadoAcceso resultado;
}
