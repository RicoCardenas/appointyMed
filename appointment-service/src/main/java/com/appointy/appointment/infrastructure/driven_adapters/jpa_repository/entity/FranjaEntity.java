package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.appointment.domain.model.EstadoFranja;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "franja")
@Getter
@Setter
@NoArgsConstructor
public class FranjaEntity {

    @Id
    private UUID id;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFranja estado;

    @Version
    @Column(name = "version")
    private Long version;
}
