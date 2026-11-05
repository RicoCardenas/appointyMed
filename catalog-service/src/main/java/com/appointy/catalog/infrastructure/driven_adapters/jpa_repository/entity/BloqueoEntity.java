package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bloqueo")
@Getter
@Setter
@NoArgsConstructor
public class BloqueoEntity {

    @Id
    private UUID id;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
