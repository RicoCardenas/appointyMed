-- clinical-history-service: historia clinica y log de acceso. Sin FK hacia otros schemas: paciente_id, profesional_id
-- y cita_id son referencias logicas a auth, catalog y appointment (RNF-15). Los instantes estan en UTC.

CREATE TABLE entrada_clinica (
    id              UUID        PRIMARY KEY,
    paciente_id     UUID        NOT NULL,
    profesional_id  UUID        NOT NULL,
    cita_id         UUID        NOT NULL,
    motivo          TEXT        NOT NULL,
    diagnostico     TEXT        NOT NULL,
    notas           TEXT,
    indicaciones    TEXT,
    corrige_a_id    UUID,
    creado_en       TIMESTAMP   NOT NULL,
    CONSTRAINT fk_entrada_corrige FOREIGN KEY (corrige_a_id) REFERENCES entrada_clinica (id)
);

CREATE INDEX ix_entrada_paciente ON entrada_clinica (paciente_id, creado_en);
-- Una sola entrada original por cita; las correcciones (corrige_a_id no nulo) no cuentan. Red de seguridad del caso de uso.
CREATE UNIQUE INDEX uq_entrada_original_cita ON entrada_clinica (cita_id) WHERE corrige_a_id IS NULL;

-- RF-30. paciente_id y entrada_id pueden ser nulos cuando el acceso se rechaza antes de conocerlos.
CREATE TABLE log_acceso_clinico (
    id          UUID        PRIMARY KEY,
    usuario_id  UUID        NOT NULL,
    rol         VARCHAR(20) NOT NULL,
    paciente_id UUID,
    accion      VARCHAR(10) NOT NULL,
    entrada_id  UUID,
    fecha       TIMESTAMP   NOT NULL,
    resultado   VARCHAR(10) NOT NULL,
    CONSTRAINT ck_log_accion    CHECK (accion IN ('LECTURA', 'ESCRITURA')),
    CONSTRAINT ck_log_resultado CHECK (resultado IN ('PERMITIDO', 'DENEGADO'))
);

CREATE INDEX ix_log_paciente ON log_acceso_clinico (paciente_id, fecha);
CREATE INDEX ix_log_usuario  ON log_acceso_clinico (usuario_id, fecha);

-- Append-only a nivel de base de datos (RF-29, RF-30): ni la aplicacion ni un descuido pueden actualizar o borrar filas.
CREATE FUNCTION rechazar_cambios() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'La tabla % es de solo insercion', TG_TABLE_NAME;
END;
$$;

CREATE TRIGGER tr_entrada_inmutable BEFORE UPDATE OR DELETE ON entrada_clinica
    FOR EACH ROW EXECUTE FUNCTION rechazar_cambios();
CREATE TRIGGER tr_log_inmutable BEFORE UPDATE OR DELETE ON log_acceso_clinico
    FOR EACH ROW EXECUTE FUNCTION rechazar_cambios();
