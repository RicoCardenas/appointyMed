-- Crea la base `appointy`, un schema y un usuario por servicio (RNF-15).
-- Idempotente: se puede repetir sin error. Las claves llegan por variables de psql,
-- no se guardan en el repositorio.
--
-- Uso (como superusuario de PostgreSQL):
--   sudo -u postgres psql \
--     -v pwd_auth=... -v pwd_catalog=... -v pwd_appointment=... \
--     -v pwd_clinical=... -v pwd_notification=... \
--     -f db/init.sql

SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', r.rol, r.pwd)
FROM (VALUES
    ('auth_svc',         :'pwd_auth'),
    ('catalog_svc',      :'pwd_catalog'),
    ('appointment_svc',  :'pwd_appointment'),
    ('clinical_svc',     :'pwd_clinical'),
    ('notification_svc', :'pwd_notification')
) AS r(rol, pwd)
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = r.rol)
\gexec

SELECT 'CREATE DATABASE appointy'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'appointy')
\gexec

\connect appointy

-- Nadie crea objetos en public; cada rol solo ve su propio schema.
REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE SCHEMA IF NOT EXISTS auth         AUTHORIZATION auth_svc;
CREATE SCHEMA IF NOT EXISTS catalog      AUTHORIZATION catalog_svc;
CREATE SCHEMA IF NOT EXISTS appointment  AUTHORIZATION appointment_svc;
CREATE SCHEMA IF NOT EXISTS clinical     AUTHORIZATION clinical_svc;
CREATE SCHEMA IF NOT EXISTS notification AUTHORIZATION notification_svc;

ALTER ROLE auth_svc         SET search_path = auth;
ALTER ROLE catalog_svc      SET search_path = catalog;
ALTER ROLE appointment_svc  SET search_path = appointment;
ALTER ROLE clinical_svc     SET search_path = clinical;
ALTER ROLE notification_svc SET search_path = notification;
