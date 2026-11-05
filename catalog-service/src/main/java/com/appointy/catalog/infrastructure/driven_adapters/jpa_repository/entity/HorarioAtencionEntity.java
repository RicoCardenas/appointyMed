package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "horario_atencion")
@Getter
@Setter
@NoArgsConstructor
public class HorarioAtencionEntity {

    @Id
    private UUID id;

    @Column(name = "profesional_id", nullable = false)
    private UUID profesionalId;

    @Column(name = "centro_id", nullable = false)
    private UUID centroId;

    @Column(name = "dia_semana", nullable = false)
    private int diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracion_franja_min", nullable = false)
    private int duracionFranjaMin;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
