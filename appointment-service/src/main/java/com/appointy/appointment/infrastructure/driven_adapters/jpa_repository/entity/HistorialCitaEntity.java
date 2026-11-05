package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.appointment.domain.model.EstadoCita;
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

/** Append-only (RNF-13): {@code @Immutable} hace que Hibernate nunca emita un UPDATE sobre esta tabla. */
@Entity
@Immutable
@Table(name = "historial_cita")
@Getter
@Setter
@NoArgsConstructor
public class HistorialCitaEntity {

    @Id
    private UUID id;

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoCita estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoCita estadoNuevo;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "rol_usuario", nullable = false)
    private String rolUsuario;

    @Column(name = "franja_anterior_id")
    private UUID franjaAnteriorId;

    @Column(name = "franja_nueva_id")
    private UUID franjaNuevaId;

    @Column(name = "detalle")
    private String detalle;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "fecha", nullable = false)
    private Instant fecha;
}
