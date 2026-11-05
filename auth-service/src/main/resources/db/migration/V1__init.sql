-- Schema auth: usuarios, perfil del paciente y tokens revocados (cierre de sesion).
-- El schema y el usuario de BD los crea db/init.sql; Flyway trabaja dentro del schema auth.

CREATE TABLE usuario (
    id             UUID         PRIMARY KEY,
    correo         VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP    NOT NULL,
    actualizado_en TIMESTAMP    NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_correo_minusculas CHECK (correo = lower(correo)),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('PACIENTE', 'PROFESIONAL', 'ADMINISTRATIVO'))
);

CREATE TABLE paciente_perfil (
    usuario_id       UUID         PRIMARY KEY REFERENCES usuario (id),
    cedula           VARCHAR(20)  NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    telefono         VARCHAR(20),
    direccion        VARCHAR(200),
    ciudad           VARCHAR(100),
    CONSTRAINT uq_paciente_cedula UNIQUE (cedula)
);

CREATE TABLE token_revocado (
    jti       VARCHAR(64) PRIMARY KEY,
    expira_en TIMESTAMP   NOT NULL
);

CREATE INDEX idx_token_revocado_expira_en ON token_revocado (expira_en);
