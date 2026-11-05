package com.appointy.auth.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.auth.domain.model.Rol;
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
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(name = "correo", nullable = false)
    private String correo;

    @Column(name = "password_hash", nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
