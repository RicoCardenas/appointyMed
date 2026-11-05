package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parametro_servicio")
@Getter
@Setter
@NoArgsConstructor
public class ParametroServicioEntity {

    @Id
    @Column(name = "clave")
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "descripcion")
    private String descripcion;
}
