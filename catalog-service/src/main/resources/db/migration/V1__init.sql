-- Schema catalog: especialidades, centros, profesionales, horarios, bloqueos y parametros.
-- Las referencias a otros servicios (usuario_id) son logicas: sin FK entre schemas.

CREATE TABLE especialidad (
    id          UUID         PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_especialidad_nombre UNIQUE (nombre)
);

CREATE TABLE centro (
    id        UUID         PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    ciudad    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(20),
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE profesional (
    id               UUID         PRIMARY KEY,
    usuario_id       UUID         NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    registro_medico  VARCHAR(30)  NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_profesional_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_profesional_registro UNIQUE (registro_medico)
);

CREATE TABLE profesional_especialidad (
    profesional_id UUID NOT NULL REFERENCES profesional (id),
    especialidad_id UUID NOT NULL REFERENCES especialidad (id),
    PRIMARY KEY (profesional_id, especialidad_id)
);

CREATE TABLE profesional_centro (
    profesional_id UUID NOT NULL REFERENCES profesional (id),
    centro_id      UUID NOT NULL REFERENCES centro (id),
    PRIMARY KEY (profesional_id, centro_id)
);

-- Solo se define horario donde el profesional realmente atiende (RN-06).
CREATE TABLE horario_atencion (
    id                  UUID    PRIMARY KEY,
    profesional_id      UUID    NOT NULL,
    centro_id           UUID    NOT NULL,
    dia_semana          INTEGER NOT NULL,
    hora_inicio         TIME    NOT NULL,
    hora_fin            TIME    NOT NULL,
    duracion_franja_min INTEGER NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_horario_dia CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_horas CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_horario_duracion CHECK (duracion_franja_min > 0),
    CONSTRAINT fk_horario_profesional_centro FOREIGN KEY (profesional_id, centro_id)
        REFERENCES profesional_centro (profesional_id, centro_id)
);

CREATE INDEX idx_horario_profesional_dia ON horario_atencion (profesional_id, dia_semana);

CREATE TABLE bloqueo (
    id             UUID         PRIMARY KEY,
    profesional_id UUID         NOT NULL,
    centro_id      UUID         NOT NULL,
    fecha_inicio   DATE         NOT NULL,
    fecha_fin      DATE         NOT NULL,
    motivo         VARCHAR(200) NOT NULL,
    creado_por     UUID         NOT NULL,
    creado_en      TIMESTAMP    NOT NULL,
    CONSTRAINT ck_bloqueo_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT fk_bloqueo_profesional_centro FOREIGN KEY (profesional_id, centro_id)
        REFERENCES profesional_centro (profesional_id, centro_id)
);

CREATE INDEX idx_bloqueo_profesional ON bloqueo (profesional_id, fecha_inicio);

CREATE TABLE parametro_servicio (
    clave       VARCHAR(60)  PRIMARY KEY,
    valor       VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200)
);

INSERT INTO parametro_servicio (clave, valor, descripcion) VALUES
    ('anticipacion_minima_horas', '2',  'Horas minimas de anticipacion para agendar, modificar o cancelar'),
    ('limite_cancelacion_horas',  '24', 'Horas antes de la cita hasta las que se puede cancelar'),
    ('horizonte_max_dias',        '60', 'Dias hacia adelante en que se puede agendar y se generan franjas'),
    ('duracion_franja_min',       '30', 'Duracion por defecto de una franja en minutos');
