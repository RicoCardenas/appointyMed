package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.appointment.domain.model.EstadoCita;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cita")
@Getter
@Setter
@NoArgsConstructor
public class CitaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    @Column(name = "especialidad_id", nullable = false)
    private UUID especialidadId;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "franja_id", nullable = false)
    private UUID franjaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "modificado_en", nullable = false)
    private Instant modificadoEn;

    @Version
    @Column(name = "version")
    private Long version;
}
