-- notification-service: registro de correos. cita_id es una referencia logica a appointment (RNF-15, sin FK entre schemas).
-- Los instantes estan en UTC.

CREATE TABLE notificacion (
    id                  UUID         PRIMARY KEY,
    cita_id             UUID         NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,
    destinatario_correo VARCHAR(254) NOT NULL,
    asunto              VARCHAR(200) NOT NULL,
    cuerpo              TEXT         NOT NULL,
    estado              VARCHAR(10)  NOT NULL,
    intentos            INTEGER      NOT NULL DEFAULT 0,
    ultimo_error        VARCHAR(100),
    programada_para     TIMESTAMP    NOT NULL,
    enviada_en          TIMESTAMP,
    creado_en           TIMESTAMP    NOT NULL,
    CONSTRAINT ck_notificacion_tipo    CHECK (tipo IN ('CONFIRMACION', 'REPROGRAMACION', 'CANCELACION', 'RECORDATORIO')),
    CONSTRAINT ck_notificacion_estado  CHECK (estado IN ('PENDIENTE', 'ENVIADA', 'FALLIDA', 'CANCELADA')),
    CONSTRAINT ck_notificacion_intentos CHECK (intentos >= 0),
    -- Idempotencia (RNF-16): la misma cita, tipo y momento no se registra dos veces.
    CONSTRAINT uq_notificacion_clave   UNIQUE (cita_id, tipo, programada_para)
);

CREATE INDEX ix_notificacion_cita     ON notificacion (cita_id);
CREATE INDEX ix_notificacion_estado   ON notificacion (estado, tipo, programada_para);
CREATE INDEX ix_notificacion_creado   ON notificacion (creado_en DESC);
