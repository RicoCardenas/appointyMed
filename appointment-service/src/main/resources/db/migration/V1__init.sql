-- appointment-service: franjas, citas e historial. Sin FK hacia otros schemas: profesional_id, centro_id,
-- especialidad_id y paciente_id son referencias logicas a catalog y auth (RNF-15). La fecha y la hora de la franja
-- son las locales de la clinica; los instantes (creado_en, fecha del historial) estan en UTC.

CREATE TABLE franja (
    id              UUID        PRIMARY KEY,
    profesional_id  UUID        NOT NULL,
    centro_id       UUID        NOT NULL,
    fecha           DATE        NOT NULL,
    hora_inicio     TIME        NOT NULL,
    hora_fin        TIME        NOT NULL,
    estado          VARCHAR(15) NOT NULL DEFAULT 'DISPONIBLE',
    version         BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT ck_franja_horas  CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_franja_estado CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'BLOQUEADA')),
    -- Hace idempotente la generacion de franjas (RNF-16)
    CONSTRAINT uq_franja_horario UNIQUE (profesional_id, centro_id, fecha, hora_inicio)
);

-- Consulta de disponibilidad (RNF-06)
CREATE INDEX ix_franja_disponibilidad ON franja (profesional_id, fecha, estado);

CREATE TABLE cita (
    id              UUID        PRIMARY KEY,
    paciente_id     UUID        NOT NULL,
    profesional_id  UUID        NOT NULL,
    especialidad_id UUID        NOT NULL,
    centro_id       UUID        NOT NULL,
    franja_id       UUID        NOT NULL,
    estado          VARCHAR(15) NOT NULL,
    creado_en       TIMESTAMP   NOT NULL,
    modificado_en   TIMESTAMP   NOT NULL,
    version         BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT fk_cita_franja  FOREIGN KEY (franja_id) REFERENCES franja (id),
    -- MODIFICADA no es un estado: el cambio de franja queda en historial_cita (decision 3)
    CONSTRAINT ck_cita_estado  CHECK (estado IN ('CONFIRMADA', 'CANCELADA', 'COMPLETADA', 'NO_ASISTIDA'))
);

-- Red de seguridad: nunca dos citas confirmadas para la misma franja (RNF-01). La defensa principal es el bloqueo.
CREATE UNIQUE INDEX uq_cita_franja_confirmada ON cita (franja_id) WHERE estado = 'CONFIRMADA';
CREATE INDEX ix_cita_paciente    ON cita (paciente_id, estado);
CREATE INDEX ix_cita_profesional ON cita (profesional_id, estado);
CREATE INDEX ix_cita_franja      ON cita (franja_id);

-- Append-only (RNF-13): la aplicacion solo inserta
CREATE TABLE historial_cita (
    id                UUID         PRIMARY KEY,
    cita_id           UUID         NOT NULL,
    estado_anterior   VARCHAR(15),
    estado_nuevo      VARCHAR(15)  NOT NULL,
    usuario_id        UUID         NOT NULL,
    rol_usuario       VARCHAR(20)  NOT NULL,
    franja_anterior_id UUID,
    franja_nueva_id   UUID,
    detalle           VARCHAR(300),
    fecha             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_historial_cita FOREIGN KEY (cita_id) REFERENCES cita (id)
);
CREATE INDEX ix_historial_cita ON historial_cita (cita_id, fecha);
