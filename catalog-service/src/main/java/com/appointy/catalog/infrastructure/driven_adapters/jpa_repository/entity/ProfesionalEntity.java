package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profesional")
@Getter
@Setter
@NoArgsConstructor
public class ProfesionalEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "registro_medico", nullable = false)
    private String registroMedico;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
