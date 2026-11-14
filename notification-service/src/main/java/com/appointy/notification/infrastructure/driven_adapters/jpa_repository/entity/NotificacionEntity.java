package com.appointy.notification.infrastructure.driven_adapters.jpa_repository.entity;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.TipoNotificacion;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionEntity {

    @Id
    private UUID id;

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacion tipo;

    @Column(name = "destinatario_correo", nullable = false)
    private String destinatarioCorreo;

    @Column(name = "asunto", nullable = false)
    private String asunto;

    @Column(name = "cuerpo", nullable = false)
    private String cuerpo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNotificacion estado;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "programada_para", nullable = false)
    private Instant programadaPara;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "enviada_en")
    private Instant enviadaEn;

    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
